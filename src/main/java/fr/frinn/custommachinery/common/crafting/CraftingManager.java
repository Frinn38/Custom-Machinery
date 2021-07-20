package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.common.crafting.requirements.IChanceableRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.IDelayedRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.ITickableRequirement;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.sync.DoubleSyncable;
import fr.frinn.custommachinery.common.network.sync.IntegerSyncable;
import fr.frinn.custommachinery.common.network.sync.StringSyncable;
import fr.frinn.custommachinery.common.util.Comparators;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CraftingManager implements INBTSerializable<CompoundNBT> {

    private CustomMachineTile tile;
    private Random rand;
    private CustomMachineRecipe currentRecipe;
    public double recipeProgressTime = 0;
    public int recipeTotalTime = 0;
    private List<IRequirement<?>> processedRequirements;
    private CraftingContext context;
    private int refreshModifiersCooldown = 20;

    private Map<Double, IDelayedRequirement<IMachineComponent>> delayedRequirements = new HashMap<>();

    private STATUS status;
    private STATUS prevStatus;
    private PHASE phase;

    private ITextComponent errorMessage = StringTextComponent.EMPTY;

    public CraftingManager(CustomMachineTile tile) {
        this.tile = tile;
        this.rand = tile.getWorld() != null ? tile.getWorld().rand : new Random();
        this.status = STATUS.IDLE;
        this.processedRequirements = new ArrayList<>();
    }

    public void tick() {
        if(this.context == null)
            this.context = new CraftingContext(this.tile);
        if(this.tile.isPaused() && this.status != STATUS.PAUSED) {
            this.prevStatus = this.status;
            this.status = STATUS.PAUSED;
            notifyStatusChanged();
        }
        if(!this.tile.isPaused() && this.status == STATUS.PAUSED) {
            this.status = this.prevStatus;
            notifyStatusChanged();
        }
        if(this.status == STATUS.PAUSED)
            return;
        if(this.currentRecipe == null) {
            this.findRecipe().ifPresent(recipe -> {
                this.currentRecipe = recipe;
                this.context.setRecipe(recipe);
                this.context.refreshModifiers(this.tile);
                this.refreshModifiersCooldown = 20;
                this.delayedRequirements = this.currentRecipe.getRequirements()
                        .stream()
                        .filter(requirement -> requirement instanceof IDelayedRequirement)
                        .map(requirement -> (IDelayedRequirement<IMachineComponent>)requirement).collect(Collectors.toMap(IDelayedRequirement::getDelay, requirement -> requirement));
                this.recipeTotalTime = this.currentRecipe.getRecipeTime();
                this.phase = PHASE.STARTING;
                this.setRunning();
            });
        }
        if(this.currentRecipe != null) {
            if(this.refreshModifiersCooldown-- <= 0) {
                this.refreshModifiersCooldown = 20;
                this.context.refreshModifiers(this.tile);
            }
            if(this.phase == PHASE.STARTING) {
                for (IRequirement<?> requirement : this.currentRecipe.getRequirements()) {
                    if (!this.processedRequirements.contains(requirement)) {
                        IMachineComponent component = this.tile.componentManager.getComponent(requirement.getComponentType()).orElseThrow(() -> new ComponentNotFoundException(this.currentRecipe, this.tile.getMachine(), requirement.getType()));
                        if (requirement instanceof IChanceableRequirement && ((IChanceableRequirement<IMachineComponent>) requirement).testChance(component, this.rand, this.context)) {
                            this.processedRequirements.add(requirement);
                            continue;
                        }
                        CraftingResult result = ((IRequirement<IMachineComponent>)requirement).processStart(component, this.context);
                        if (!result.isSuccess()) {
                            this.setErrored(result.getMessage());
                            break;
                        } else this.processedRequirements.add(requirement);
                    }
                }

                if (this.processedRequirements.size() == this.currentRecipe.getRequirements().size()) {
                    this.setRunning();
                    this.phase = PHASE.CRAFTING_TICKABLE;
                    this.processedRequirements.clear();
                }
            }
            if(this.phase == PHASE.CRAFTING_TICKABLE) {
                List<ITickableRequirement<IMachineComponent>> tickableRequirements = this.currentRecipe.getRequirements()
                        .stream()
                        .filter(requirement -> requirement instanceof ITickableRequirement<?>)
                        .map(requirement -> (ITickableRequirement<IMachineComponent>) requirement)
                        .collect(Collectors.toList());

                for (ITickableRequirement<IMachineComponent> tickableRequirement : tickableRequirements) {
                    if (!this.processedRequirements.contains(tickableRequirement)) {
                        IMachineComponent component = this.tile.componentManager.getComponent(tickableRequirement.getComponentType()).orElseThrow(() -> new ComponentNotFoundException(this.currentRecipe, this.tile.getMachine(), tickableRequirement.getType()));
                        if (tickableRequirement instanceof IChanceableRequirement && ((IChanceableRequirement<IMachineComponent>) tickableRequirement).testChance(component, this.rand, this.context)) {
                            this.processedRequirements.add(tickableRequirement);
                            continue;
                        }
                        CraftingResult result = tickableRequirement.processTick(component, this.context);
                        if (!result.isSuccess()) {
                            this.setErrored(result.getMessage());
                            break;
                        } else this.processedRequirements.add(tickableRequirement);
                    }
                }

                if (this.processedRequirements.size() == tickableRequirements.size()) {
                    this.recipeProgressTime += this.context.getModifiedSpeed();
                    this.context.setRecipeProgressTime(this.recipeProgressTime);
                    this.setRunning();
                    this.processedRequirements.clear();
                }
                this.phase = PHASE.CRAFTING_DELAYED;
            }
            if(this.phase == PHASE.CRAFTING_DELAYED) {
                for(Iterator<Map.Entry<Double, IDelayedRequirement<IMachineComponent>>> iterator = this.delayedRequirements.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<Double, IDelayedRequirement<IMachineComponent>> entry = iterator.next();
                    if(this.recipeProgressTime / this.recipeTotalTime >= entry.getKey()) {
                        IMachineComponent component = this.tile.componentManager.getComponent(entry.getValue().getComponentType()).orElseThrow(() -> new ComponentNotFoundException(this.currentRecipe, this.tile.getMachine(), entry.getValue().getType()));
                        CraftingResult result = entry.getValue().execute(component, this.context);
                        if(!result.isSuccess()) {
                            this.setErrored(result.getMessage());
                            break;
                        } else iterator.remove();
                    }
                }

                if(this.delayedRequirements.keySet().stream().allMatch(delay -> this.recipeProgressTime / this.recipeTotalTime < delay))
                    if (this.recipeProgressTime >= this.recipeTotalTime)
                        this.phase = PHASE.ENDING;
                    else this.phase = PHASE.CRAFTING_TICKABLE;
            }
            if(this.phase == PHASE.ENDING) {
                for(IRequirement<?> requirement : this.currentRecipe.getRequirements()) {
                    if(!this.processedRequirements.contains(requirement)) {
                        IMachineComponent component = this.tile.componentManager.getComponent(requirement.getComponentType()).orElseThrow(() -> new ComponentNotFoundException(this.currentRecipe, this.tile.getMachine(), requirement.getType()));
                        if(requirement instanceof IChanceableRequirement && ((IChanceableRequirement) requirement).testChance(component, this.rand, this.context)) {
                            this.processedRequirements.add(requirement);
                            continue;
                        }
                        CraftingResult result = ((IRequirement)requirement).processEnd(component, this.context);
                        if(!result.isSuccess()) {
                            this.setErrored(result.getMessage());
                            break;
                        }
                        else this.processedRequirements.add(requirement);
                    }
                }

                if(this.processedRequirements.size() == this.currentRecipe.getRequirements().size()) {
                    this.currentRecipe = null;
                    this.recipeProgressTime = 0;
                    this.context = null;
                    this.processedRequirements.clear();
                }
            }
        }
        else this.setIdle();
    }

    private Optional<CustomMachineRecipe> findRecipe() {
        if(this.tile.getWorld() == null)
            return Optional.empty();
        return this.tile.getWorld().getRecipeManager()
                .getRecipesForType(Registration.CUSTOM_MACHINE_RECIPE)
                .stream()
                .filter(recipe -> recipe.matches(this.tile, this.context))
                .max(Comparators.CUSTOM_MACHINE_RECIPE_COMPARATOR);
    }

    public ITextComponent getErrorMessage() {
        return this.errorMessage;
    }

    public void setIdle() {
        if(this.status != STATUS.IDLE) {
            this.status = STATUS.IDLE;
            this.errorMessage = StringTextComponent.EMPTY;
            this.tile.markDirty();
            notifyStatusChanged();
        }
    }

    public void setErrored(ITextComponent message) {
        if(this.status != STATUS.ERRORED || !this.errorMessage.equals(message)) {
            this.status = STATUS.ERRORED;
            this.errorMessage = message;
            this.tile.markDirty();
            notifyStatusChanged();
        }
    }

    public void setRunning() {
        if(this.status != STATUS.RUNNING) {
            this.status = STATUS.RUNNING;
            this.errorMessage = StringTextComponent.EMPTY;
            this.tile.markDirty();
            notifyStatusChanged();
        }
    }

    private void notifyStatusChanged() {
        if(this.tile.getWorld() != null)
            this.tile.getWorld().notifyBlockUpdate(this.tile.getPos(), this.tile.getBlockState(), this.tile.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public STATUS getStatus() {
        return this.status;
    }

    public CustomMachineRecipe getCurrentRecipe() {
        return this.currentRecipe;
    }

    public void addProbeInfo(IProbeInfo info) {
        info.text(new TranslationTextComponent("custommachinery.craftingstatus." + this.status.toString().toLowerCase(Locale.ENGLISH)));
        switch (this.status) {
            case RUNNING:
                info.progress((int)this.recipeProgressTime, this.recipeTotalTime, info.defaultProgressStyle().suffix("/" + this.recipeTotalTime));
                break;
            case ERRORED:
                info.text(this.errorMessage);
                break;
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("status", this.status.toString());
        nbt.putString("message", this.errorMessage.getString());
        nbt.putDouble("recipeProgressTime", this.recipeProgressTime);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if(nbt.contains("status", Constants.NBT.TAG_STRING))
            this.status = STATUS.value(nbt.getString("status"));
        if(nbt.contains("message", Constants.NBT.TAG_STRING))
            this.errorMessage = ITextComponent.getTextComponentOrEmpty(nbt.getString("message"));
        if(nbt.contains("recipeProgressTime", Constants.NBT.TAG_DOUBLE))
            this.recipeProgressTime = nbt.getDouble("recipeProgressTime");
    }

    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(DoubleSyncable.create(() -> this.recipeProgressTime, recipeProgressTime -> this.recipeProgressTime = recipeProgressTime));
        container.accept(IntegerSyncable.create(() -> this.recipeTotalTime, recipeTotalTime -> this.recipeTotalTime = recipeTotalTime));
        container.accept(StringSyncable.create(() -> this.status.toString(), status -> this.status = STATUS.value(status)));
        container.accept(StringSyncable.create(() -> this.errorMessage.getString(), errorMessage -> this.errorMessage = ITextComponent.getTextComponentOrEmpty(errorMessage)));
    }

    public enum PHASE {
        STARTING,
        CRAFTING_TICKABLE,
        CRAFTING_DELAYED,
        ENDING;

        public static PHASE value(String string) {
            return valueOf(string.toUpperCase(Locale.ENGLISH));
        }
    }

    public enum STATUS {
        IDLE,
        RUNNING,
        ERRORED,
        PAUSED;

        public static STATUS value(String string) {
            return valueOf(string.toUpperCase(Locale.ENGLISH));
        }
    }
}
