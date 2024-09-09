package fr.frinn.custommachinery.common.crafting.machine;

import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingContext.Mutable;
import fr.frinn.custommachinery.common.network.syncable.DoubleSyncable;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.crafting.RequirementList;
import fr.frinn.custommachinery.impl.crafting.RequirementList.RequirementWithFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class MachineProcessorCore implements ISyncableStuff {

    private final MachineProcessor processor;
    private final MachineTile tile;
    private final Random rand = Utils.RAND;
    private final MachineRecipeFinder recipeFinder;

    @Nullable
    private RecipeHolder<CustomMachineRecipe> currentRecipe;
    //Recipe that was processed when the machine was unloaded, and we need to resume
    private ResourceLocation futureRecipeID;
    private CraftingContext context;
    private double recipeProgressTime = 0;
    private int recipeTotalTime = 0;
    private boolean searchImmediately = false;
    private Phase phase = Phase.CONDITIONS;
    private boolean machineInventoryChanged = true;
    @Nullable
    private Component error = null;
    private boolean isLastRecipeTick = false;

    private RequirementList<?> requirementList;
    private final List<RequirementWithFunction> currentProcessRequirements = new ArrayList<>();

    public MachineProcessorCore(MachineProcessor processor, MachineTile tile, int baseCooldown, int core) {
        this.processor = processor;
        this.tile = tile;
        this.recipeFinder = new MachineRecipeFinder(tile, processor, baseCooldown, new Mutable(tile, tile.getUpgradeManager(), core - 1), core);
    }

    @Nullable
    public RecipeHolder<CustomMachineRecipe> getCurrentRecipe() {
        return this.currentRecipe;
    }

    @Nullable
    public Component getError() {
        return this.error;
    }

    public double getRecipeProgressTime() {
        return this.recipeProgressTime;
    }

    public double getRecipeTotalTime() {
        return this.recipeTotalTime;
    }

    public void init() {
        //Search for previous recipe
        if(this.futureRecipeID != null && this.tile.getLevel() != null) {
            this.tile.getLevel().getRecipeManager()
                    .byKey(this.futureRecipeID)
                    .filter(holder -> holder.value() instanceof CustomMachineRecipe)
                    .map(holder -> (RecipeHolder<CustomMachineRecipe>)holder)
                    .ifPresent(this::setRecipe);
            this.futureRecipeID = null;
        }
        this.recipeFinder.init();
    }

    public void tick() {
        if(this.currentRecipe == null) {
            this.recipeFinder.findRecipe(this.searchImmediately).ifPresent(this::setRecipe);
            this.searchImmediately = false;
            this.machineInventoryChanged = false;
        }

        if(this.currentRecipe != null) {
            if(this.phase == Phase.CONDITIONS)
                this.checkConditions();

            if(this.phase == Phase.PROCESS)
                this.processRequirements();

            if(this.phase == Phase.PROCESS_TICK)
                this.processTickRequirements();

            if(this.recipeProgressTime >= this.recipeTotalTime) {
                if(this.isLastRecipeTick) {
                    this.isLastRecipeTick = false;
                    this.currentRecipe = null;
                    this.recipeProgressTime = 0.0D;
                    this.context = null;
                    this.recipeFinder.findRecipe(true).ifPresent(this::setRecipe);
                } else
                    this.isLastRecipeTick = true;
            }
        }
    }

    private void checkConditions() {
        for(RequirementWithFunction requirement : this.requirementList.getWorldConditions()) {
            CraftingResult result = requirement.process(this.tile.getComponentManager(), this.context);
            if(!result.isSuccess()) {
                this.setError(result.getMessage());
                return;
            }
        }

        if(this.machineInventoryChanged) {
            this.machineInventoryChanged = false;
            for(RequirementWithFunction requirement : this.requirementList.getInventoryConditions()) {
                CraftingResult result = requirement.process(this.tile.getComponentManager(), this.context);
                if(!result.isSuccess()) {
                    this.setError(result.getMessage());
                    return;
                }
            }
        }

        this.setRunning();
        this.phase = Phase.PROCESS;
    }

    private void processRequirements() {
        if(this.currentProcessRequirements.isEmpty()) {
            this.requirementList.getProcessRequirements().entrySet().removeIf(entry -> {
                //if the recipe is at last tick process all remaining requirements
                //Else process only requirements that have a delay lower than the current progress
                if(entry.getKey() <= this.recipeProgressTime / this.recipeTotalTime || this.isLastRecipeTick) {
                    this.currentProcessRequirements.addAll(entry.getValue());
                    return true;
                }
                return false;
            });
        }

        for(Iterator<RequirementWithFunction> iterator = this.currentProcessRequirements.iterator(); iterator.hasNext(); ) {
            RequirementWithFunction requirement = iterator.next();
            if(!requirement.requirement().shouldSkip(this.tile.getComponentManager(), this.rand, this.context)) {
                CraftingResult result = requirement.process(this.tile.getComponentManager(), this.context);
                if(!result.isSuccess()) {
                    if(this.currentRecipe.value().shouldResetOnError())
                        this.reset();
                    else
                        this.setError(result.getMessage());
                    return;
                }
            }
            iterator.remove();
        }

        this.setRunning();
        this.phase = Phase.PROCESS_TICK;
    }

    private void processTickRequirements() {
        if(this.currentProcessRequirements.isEmpty())
            this.currentProcessRequirements.addAll(this.requirementList.getTickableRequirements());

        for(Iterator<RequirementWithFunction> iterator = this.currentProcessRequirements.iterator(); iterator.hasNext(); ) {
            RequirementWithFunction requirement = iterator.next();
            if(!requirement.requirement().shouldSkip(this.tile.getComponentManager(), this.rand, this.context)) {
                CraftingResult result = requirement.process(this.tile.getComponentManager(), this.context);
                if(!result.isSuccess()) {
                    if(this.currentRecipe.value().shouldResetOnError())
                        this.reset();
                    else
                        this.setError(result.getMessage());
                    return;
                }
            }
            iterator.remove();
        }

        this.setRunning();
        this.phase = Phase.CONDITIONS;
        this.recipeProgressTime += this.context.getModifiedSpeed();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setRecipe(@NotNull RecipeHolder<CustomMachineRecipe> recipe) {
        this.currentRecipe = recipe;
        this.context = new CraftingContext(this.tile, this.tile.getUpgradeManager(), recipe, () -> this.recipeProgressTime, this.processor.getCores().indexOf(this));
        this.recipeTotalTime = this.currentRecipe.value().getRecipeTime();
        this.requirementList = new RequirementList<>();
        this.currentRecipe.value().getRequirements().forEach(requirement -> {
            this.requirementList.setCurrentRequirement(requirement);
            requirement.requirement().gatherRequirements((RequirementList)this.requirementList);
        });
        this.phase = Phase.CONDITIONS;
    }

    private void setRunning() {
        this.error = null;
        this.processor.setRunning();
    }

    private void setError(Component error) {
        this.error = error;
        this.processor.setError(error);
    }

    public void reset() {
        this.currentRecipe = null;
        this.futureRecipeID = null;
        this.recipeProgressTime = 0;
        this.recipeTotalTime = 0;
        this.requirementList = null;
        this.context = null;
        this.phase = Phase.CONDITIONS;
    }

    public void setSearchImmediately() {
        if(this.currentRecipe == null)
            this.searchImmediately = true;
    }

    public void setMachineInventoryChanged() {
        this.recipeFinder.setInventoryChanged(true);
        this.machineInventoryChanged = true;
    }

    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        if(this.currentRecipe != null)
            nbt.putString("recipe", this.currentRecipe.id().toString());
        nbt.putString("phase", this.phase.toString());
        nbt.putDouble("recipeProgressTime", this.recipeProgressTime);
        return nbt;
    }

    public void deserialize(CompoundTag nbt) {
        if(nbt.contains("recipe", Tag.TAG_STRING))
            this.futureRecipeID = ResourceLocation.parse(nbt.getString("recipe"));
        if(nbt.contains("phase", Tag.TAG_STRING))
            this.phase = Phase.valueOf(nbt.getString("phase"));
        if(nbt.contains("recipeProgressTime", Tag.TAG_DOUBLE))
            this.recipeProgressTime = nbt.getDouble("recipeProgressTime");
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(DoubleSyncable.create(() -> this.recipeProgressTime, recipeProgressTime -> this.recipeProgressTime = recipeProgressTime));
        container.accept(IntegerSyncable.create(() -> this.recipeTotalTime, recipeTotalTime -> this.recipeTotalTime = recipeTotalTime));
    }

    public enum Phase {
        CONDITIONS,
        PROCESS,
        PROCESS_TICK
    }
}
