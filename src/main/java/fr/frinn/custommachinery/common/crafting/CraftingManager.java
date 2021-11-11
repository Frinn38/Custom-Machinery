package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.common.crafting.requirements.IChanceableRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.IDelayedRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.ITickableRequirement;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SCraftingManagerStatusChangedPacket;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.common.util.TextComponentUtils;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.network.syncable.DoubleSyncable;
import fr.frinn.custommachinery.impl.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.impl.network.syncable.StringSyncable;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CraftingManager implements INBTSerializable<CompoundNBT> {

    private CustomMachineTile tile;
    private Random rand;
    private CustomMachineRecipe currentRecipe;
    private ResourceLocation futureRecipeID;
    public double recipeProgressTime = 0;
    public int recipeTotalTime = 0;
    private List<IRequirement<?>> processedRequirements;
    private CraftingContext context;
    private int refreshModifiersCooldown = Utils.RAND.nextInt(20);
    private int recipeCheckCooldown = Utils.RAND.nextInt(20);

    private List<IDelayedRequirement<IMachineComponent>> delayedRequirements;

    private MachineStatus status;
    private MachineStatus prevStatus;
    private PHASE phase = PHASE.STARTING;

    private ITextComponent errorMessage = StringTextComponent.EMPTY;

    public CraftingManager(CustomMachineTile tile) {
        this.tile = tile;
        this.rand = tile.getWorld() != null ? tile.getWorld().rand : new Random();
        this.status = MachineStatus.IDLE;
        this.prevStatus = this.status;
        this.processedRequirements = new ArrayList<>();
        this.delayedRequirements = new ArrayList<>();
    }

    public void tick() {
        if(this.context == null)
            this.context = new CraftingContext(this.tile);
        if(this.tile.isPaused() && this.status != MachineStatus.PAUSED) {
            this.prevStatus = this.status;
            this.status = MachineStatus.PAUSED;
            notifyStatusChanged();
        }
        if(!this.tile.isPaused() && this.status == MachineStatus.PAUSED) {
            this.status = this.prevStatus;
            notifyStatusChanged();
        }
        if(this.status == MachineStatus.PAUSED)
            return;
        if(this.futureRecipeID != null && this.tile.getWorld() != null) {
            CustomMachineRecipe recipe = (CustomMachineRecipe) this.tile.getWorld().getRecipeManager().getRecipe(this.futureRecipeID).orElse(null);
            if(recipe != null) {
                this.setRecipe(recipe);
                this.context.setRecipeProgressTime(this.recipeProgressTime);
            }
            this.futureRecipeID = null;
        }
        if(this.currentRecipe == null) {
            this.findRecipe().ifPresent(recipe -> {
                this.setRecipe(recipe);
                this.phase = PHASE.STARTING;
                this.setStatus(MachineStatus.RUNNING);
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
                            this.setStatus(MachineStatus.ERRORED, result.getMessage());
                            break;
                        } else this.processedRequirements.add(requirement);
                    }
                }

                if (this.processedRequirements.size() == this.currentRecipe.getRequirements().size()) {
                    this.setStatus(MachineStatus.RUNNING);
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
                            this.setStatus(MachineStatus.ERRORED, result.getMessage());
                            break;
                        } else this.processedRequirements.add(tickableRequirement);
                    }
                }

                if (this.processedRequirements.size() == tickableRequirements.size()) {
                    this.recipeProgressTime += this.context.getModifiedSpeed();
                    this.context.setRecipeProgressTime(this.recipeProgressTime);
                    this.setStatus(MachineStatus.RUNNING);
                    this.processedRequirements.clear();
                }
                this.phase = PHASE.CRAFTING_DELAYED;
            }
            if(this.phase == PHASE.CRAFTING_DELAYED) {
                for(Iterator<IDelayedRequirement<IMachineComponent>> iterator = this.delayedRequirements.iterator(); iterator.hasNext(); ) {
                    IDelayedRequirement<IMachineComponent> delayedRequirement = iterator.next();
                    if(this.recipeProgressTime / this.recipeTotalTime >= delayedRequirement.getDelay()) {
                        IMachineComponent component = this.tile.componentManager.getComponent(delayedRequirement.getComponentType()).orElseThrow(() -> new ComponentNotFoundException(this.currentRecipe, this.tile.getMachine(), delayedRequirement.getType()));
                        CraftingResult result = delayedRequirement.execute(component, this.context);
                        if(!result.isSuccess()) {
                            this.setStatus(MachineStatus.ERRORED, result.getMessage());
                            break;
                        } else iterator.remove();
                    }
                }

                if(this.delayedRequirements.stream().allMatch(delayedRequirement -> this.recipeProgressTime / this.recipeTotalTime < delayedRequirement.getDelay()))
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
                            this.setStatus(MachineStatus.ERRORED, result.getMessage());
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
                    this.recipeCheckCooldown = 0;
                }
            }
        }
        else this.setStatus(MachineStatus.IDLE);
    }

    private Optional<CustomMachineRecipe> findRecipe() {
        if(this.tile.getWorld() == null || this.recipeCheckCooldown-- > 0)
            return Optional.empty();

        this.recipeCheckCooldown = 20;
        return this.tile.getWorld().getRecipeManager()
                .getRecipesForType(Registration.CUSTOM_MACHINE_RECIPE)
                .stream()
                .filter(recipe -> recipe.matches(this.tile, this.context))
                .max(Comparators.RECIPE_PRIORITY_COMPARATOR);
    }

    private void setRecipe(CustomMachineRecipe recipe) {
        this.currentRecipe = recipe;
        this.context.setRecipe(recipe);
        this.context.refreshModifiers(this.tile);
        this.refreshModifiersCooldown = 20;
        this.delayedRequirements = this.currentRecipe.getRequirements()
                .stream()
                .filter(requirement -> requirement instanceof IDelayedRequirement)
                .map(requirement -> (IDelayedRequirement<IMachineComponent>)requirement)
                .filter(requirement -> requirement.getDelay() > 0 && requirement.getDelay() < 1.0)
                .collect(Collectors.toList());
        this.recipeTotalTime = this.currentRecipe.getRecipeTime();
    }

    public ITextComponent getErrorMessage() {
        return this.errorMessage;
    }

    public void setStatus(MachineStatus status) {
        this.setStatus(status, StringTextComponent.EMPTY);
    }

    public void setStatus(MachineStatus status, ITextComponent mesage) {
        if(this.status != status) {
            this.status = status;
            this.errorMessage = mesage;
            this.tile.markDirty();
            notifyStatusChanged();
        }
    }

    private void notifyStatusChanged() {
        if(this.tile.getWorld() != null && !this.tile.getWorld().isRemote()) {
            BlockPos pos = this.tile.getPos();
            NetworkManager.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.tile.getWorld().getChunkAt(pos)), new SCraftingManagerStatusChangedPacket(pos, this.status));
        }

    }

    public MachineStatus getStatus() {
        return this.status;
    }

    public void reset() {
        this.currentRecipe = null;
        this.futureRecipeID = null;
        this.setStatus(MachineStatus.IDLE);
        this.prevStatus = MachineStatus.IDLE;
        this.recipeProgressTime = 0;
        this.recipeTotalTime = 0;
        this.processedRequirements.clear();
        this.context = null;
        this.errorMessage = StringTextComponent.EMPTY;
    }

    public CustomMachineRecipe getCurrentRecipe() {
        return this.currentRecipe;
    }

    public void addProbeInfo(IProbeInfo info) {
        TranslationTextComponent status = this.status.getTranslatedName();
        switch (this.status) {
            case ERRORED:
                status.mergeStyle(TextFormatting.RED);
                break;
            case RUNNING:
                status.mergeStyle(TextFormatting.GREEN);
                break;
            case PAUSED:
                status.mergeStyle(TextFormatting.GOLD);
                break;
        }
        info.mcText(status);
        if(this.currentRecipe != null)
            info.progress((int)this.recipeProgressTime, this.recipeTotalTime, info.defaultProgressStyle().suffix("/" + this.recipeTotalTime));
        if(this.status == MachineStatus.ERRORED)
            info.text(this.errorMessage);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if(this.currentRecipe != null)
            nbt.putString("recipe", this.currentRecipe.getId().toString());
        nbt.putString("phase", this.phase.toString());
        nbt.putString("status", this.status.toString());
        nbt.putString("message", TextComponentUtils.toJsonString(this.errorMessage));
        nbt.putDouble("recipeProgressTime", this.recipeProgressTime);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if(nbt.contains("recipe", Constants.NBT.TAG_STRING))
            this.futureRecipeID = new ResourceLocation(nbt.getString("recipe"));
        if(nbt.contains("phase", Constants.NBT.TAG_STRING))
            this.phase = PHASE.value(nbt.getString("phase"));
        if(nbt.contains("status", Constants.NBT.TAG_STRING))
            this.setStatus(MachineStatus.value(nbt.getString("status")));
        if(nbt.contains("message", Constants.NBT.TAG_STRING))
            this.errorMessage = TextComponentUtils.fromJsonString(nbt.getString("message"));
        if(nbt.contains("recipeProgressTime", Constants.NBT.TAG_DOUBLE))
            this.recipeProgressTime = nbt.getDouble("recipeProgressTime");
    }

    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(DoubleSyncable.create(() -> this.recipeProgressTime, recipeProgressTime -> this.recipeProgressTime = recipeProgressTime));
        container.accept(IntegerSyncable.create(() -> this.recipeTotalTime, recipeTotalTime -> this.recipeTotalTime = recipeTotalTime));
        container.accept(StringSyncable.create(() -> this.status.toString(), status -> this.status = MachineStatus.value(status)));
        container.accept(StringSyncable.create(() -> TextComponentUtils.toJsonString(this.errorMessage), errorMessage -> this.errorMessage = TextComponentUtils.fromJsonString(errorMessage)));
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

}
