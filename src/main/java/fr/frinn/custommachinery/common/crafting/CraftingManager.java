package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.common.crafting.requirements.IChanceableRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.ITickableRequirement;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.sync.ISyncable;
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
    private CustomMachineRecipe previousRecipe;
    public int recipeProgressTime = 0;
    public int recipeTotalTime = 0; //For client GUI only
    private List<IRequirement<?>> processedRequirements;

    private STATUS status;
    private PHASE phase;

    private ITextComponent errorMessage = StringTextComponent.EMPTY;

    public CraftingManager(CustomMachineTile tile) {
        this.tile = tile;
        this.rand = tile.getWorld() != null ? tile.getWorld().rand : new Random();
        this.status = STATUS.IDLE;
        this.processedRequirements = new ArrayList<>();
    }

    public void tick() {
        if(this.currentRecipe == null) {
            if(this.previousRecipe != null && this.previousRecipe.getMachine().equals(this.tile.getMachine().getId()) && this.previousRecipe.matches(this.tile)) {
                this.currentRecipe = this.previousRecipe;
                this.recipeTotalTime = this.currentRecipe.getRecipeTime();
                this.phase = PHASE.STARTING;
                this.setRunning();
            }
            if(this.currentRecipe == null) {
                this.findRecipe().ifPresent(recipe -> {
                    this.currentRecipe = recipe;
                    this.recipeTotalTime = this.currentRecipe.getRecipeTime();
                    this.phase = PHASE.STARTING;
                    this.setRunning();
                });
            }
            if(this.currentRecipe == null)
                this.setIdle();
        }
        if(this.currentRecipe != null) {
            switch (this.phase) {
                case STARTING:
                    for(IRequirement requirement : this.currentRecipe.getRequirements()) {
                        if(!this.processedRequirements.contains(requirement)) {
                            if(requirement instanceof IChanceableRequirement && ((IChanceableRequirement) requirement).testChance(this.rand)) {
                                this.processedRequirements.add(requirement);
                                continue;
                            }
                            IMachineComponent component = this.tile.componentManager.getComponent(requirement.getComponentType()).get();
                            CraftingResult result = requirement.processStart(component);
                            if(!result.isSuccess()) {
                                this.setErrored(result.getMessage());
                                break;
                            }
                            else this.processedRequirements.add(requirement);
                        }
                    }

                    if(this.processedRequirements.size() == this.currentRecipe.getRequirements().size()) {
                        this.setRunning();
                        this.phase = PHASE.CRAFTING;
                        this.processedRequirements.clear();
                    }
                case CRAFTING:
                    List<ITickableRequirement> tickableRequirements = this.currentRecipe.getRequirements()
                            .stream()
                            .filter(requirement -> requirement instanceof ITickableRequirement)
                            .map(requirement -> (ITickableRequirement)requirement)
                            .collect(Collectors.toList());

                    for (ITickableRequirement tickableRequirement : tickableRequirements) {
                        if(!this.processedRequirements.contains(tickableRequirement)) {
                            if(tickableRequirement instanceof IChanceableRequirement && ((IChanceableRequirement) tickableRequirement).testChance(this.rand)) {
                                this.processedRequirements.add(tickableRequirement);
                                continue;
                            }
                            CraftingResult result = tickableRequirement.processTick(this.tile.componentManager.getComponent(tickableRequirement.getComponentType()).get());
                            if(!result.isSuccess()) {
                                this.setErrored(result.getMessage());
                                break;
                            }
                            else
                                this.processedRequirements.add(tickableRequirement);
                        }
                    }

                    if(this.processedRequirements.size() == tickableRequirements.size()) {
                        this.recipeProgressTime++;
                        this.setRunning();
                        this.processedRequirements.clear();
                    }
                    if(this.recipeProgressTime == this.currentRecipe.getRecipeTime())
                        this.phase = PHASE.ENDING;

                case ENDING:
                    for(IRequirement requirement : this.currentRecipe.getRequirements()) {
                        if(!this.processedRequirements.contains(requirement)) {
                            if(requirement instanceof IChanceableRequirement && ((IChanceableRequirement) requirement).testChance(this.rand)) {
                                this.processedRequirements.add(requirement);
                                continue;
                            }
                            CraftingResult result = requirement.processEnd(this.tile.componentManager.getComponentRaw(requirement.getComponentType()));
                            if(!result.isSuccess()) {
                                this.setErrored(result.getMessage());
                                break;
                            }
                            else
                                this.processedRequirements.add(requirement);
                        }
                    }

                    if(this.processedRequirements.size() == this.currentRecipe.getRequirements().size()) {
                        this.previousRecipe = this.currentRecipe;
                        this.currentRecipe = null;
                        this.recipeProgressTime = 0;
                        this.processedRequirements.clear();
                    }
            }
        }
    }

    private Optional<CustomMachineRecipe> findRecipe() {
        return this.tile.getWorld().getRecipeManager()
                .getRecipesForType(Registration.CUSTOM_MACHINE_RECIPE)
                .stream()
                .filter(recipe -> recipe.getMachine().equals(this.tile.getMachine().getId()))
                .filter(recipe -> recipe.matches(this.tile))
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
        }
    }

    public void setErrored(ITextComponent message) {
        if(this.status != STATUS.ERRORED || !this.errorMessage.equals(message)) {
            this.status = STATUS.ERRORED;
            this.errorMessage = message;
            this.tile.markDirty();
        }
    }

    public void setRunning() {
        if(this.status != STATUS.RUNNING) {
            this.status = STATUS.RUNNING;
            this.errorMessage = StringTextComponent.EMPTY;
            this.tile.markDirty();
        }
    }

    public STATUS getStatus() {
        return this.status;
    }

    public CustomMachineRecipe getCurrentRecipe() {
        return this.currentRecipe;
    }

    public void addProbeInfo(IProbeInfo info) {
        info.text(new TranslationTextComponent("custommachinery.craftingstatus." + this.status.toString().toLowerCase(Locale.ENGLISH)));
        if(this.status == STATUS.ERRORED)
            info.text(this.errorMessage);
        if(this.status == STATUS.RUNNING)
            info.progress(this.recipeProgressTime, this.recipeTotalTime, info.defaultProgressStyle().suffix("/" + this.recipeTotalTime));
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("status", this.status.toString());
        nbt.putString("message", this.errorMessage.getString());
        nbt.putInt("recipeProgressTime", this.recipeProgressTime);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if(nbt.contains("status", Constants.NBT.TAG_STRING))
            this.status = STATUS.value(nbt.getString("status"));
        if(nbt.contains("message", Constants.NBT.TAG_STRING))
            this.errorMessage = ITextComponent.getTextComponentOrEmpty(nbt.getString("message"));
        if(nbt.contains("recipeProgressTime", Constants.NBT.TAG_INT))
            this.recipeProgressTime = nbt.getInt("recipeProgressTime");
    }

    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(IntegerSyncable.create(() -> this.recipeProgressTime, recipeProgressTime -> this.recipeProgressTime = recipeProgressTime));
        container.accept(IntegerSyncable.create(() -> this.recipeTotalTime, recipeTotalTime -> this.recipeTotalTime = recipeTotalTime));
        container.accept(StringSyncable.create(() -> this.status.toString(), status -> this.status = STATUS.value(status)));
        container.accept(StringSyncable.create(() -> this.errorMessage.getString(), errorMessage -> this.errorMessage = ITextComponent.getTextComponentOrEmpty(errorMessage)));
    }

    public enum PHASE {
        STARTING,
        CRAFTING,
        ENDING;

        public static PHASE value(String string) {
            return valueOf(string.toUpperCase(Locale.ENGLISH));
        }
    }

    public enum STATUS {
        IDLE,
        RUNNING,
        ERRORED;

        public static STATUS value(String string) {
            return valueOf(string.toUpperCase(Locale.ENGLISH));
        }
    }
}
