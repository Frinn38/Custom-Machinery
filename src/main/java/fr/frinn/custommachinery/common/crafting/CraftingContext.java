package fr.frinn.custommachinery.common.crafting;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.upgrade.IMachineUpgradeManager;
import fr.frinn.custommachinery.api.upgrade.IRecipeModifier;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class CraftingContext implements ICraftingContext {

    private final MachineTile tile;
    private final IMachineUpgradeManager upgrades;
    private final RecipeHolder<? extends IMachineRecipe> recipe;
    private final Supplier<Double> progressTimeGetter;
    private final int core;
    private double baseSpeed = 1.0D;

    public CraftingContext(MachineTile tile, IMachineUpgradeManager upgrades, RecipeHolder<? extends IMachineRecipe> recipe, Supplier<Double> progressTimeGetter, int core) {
        this.tile = tile;
        this.upgrades = upgrades;
        this.recipe = recipe;
        this.progressTimeGetter = progressTimeGetter;
        this.core = core;
    }

    @Override
    public MachineTile getMachineTile() {
        return this.tile;
    }

    @Override
    public int getCurrentCore() {
        return this.core;
    }

    @Override
    public IMachineRecipe getRecipe() {
        return this.recipe.value();
    }

    @Override
    public ResourceLocation getRecipeId() {
        return this.recipe.id();
    }

    @Override
    public double getRemainingTime() {
        if(getRecipe() == null)
            return 0;
        return getRecipe().getRecipeTime() - this.progressTimeGetter.get();
    }

    @Override
    public double getBaseSpeed() {
        return this.baseSpeed;
    }

    @Override
    public void setBaseSpeed(double baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    @Override
    public double getModifiedSpeed() {
        if(getRecipe() == null)
            return this.baseSpeed;
        int baseTime = getRecipe().getRecipeTime();
        double modifiedTime = getModifiedValue(baseTime, Registration.SPEED_REQUIREMENT.get(), null, null);
        double speed = baseTime * this.baseSpeed / modifiedTime;
        return Math.max(0.01, speed);
    }

    @Override
    public long getIntegerModifiedValue(double value, IRequirement<?> requirement, @Nullable String target) {
        return Math.round(getModifiedValue(value, requirement, target));
    }

    @Override
    public long getPerTickIntegerModifiedValue(double value, IRequirement<?> requirement, @Nullable String target) {
        return Math.round(getPerTickModifiedValue(value, requirement, target));
    }

    @Override
    public double getModifiedValue(double value, IRequirement<?> requirement, @Nullable String target) {
        return getModifiedValue(value, requirement.getType(), target, requirement.getMode());
    }

    @Override
    public double getPerTickModifiedValue(double value, IRequirement<?> requirement, @Nullable String target) {
        if(this.getRemainingTime() > 0)
            return getModifiedValue(value, requirement, target) * Math.min(this.getModifiedSpeed(), this.getRemainingTime());
        return getModifiedValue(value, requirement, target) * this.getModifiedSpeed();
    }

    private double getModifiedValue(double value, RequirementType<?> type, @Nullable String target, @Nullable RequirementIOMode mode) {
        double modified = value;
        List<Pair<IRecipeModifier, Integer>> modifiers = this.upgrades.getAllModifiers();
        for(Pair<IRecipeModifier, Integer> pair : modifiers) {
            if(pair.getFirst().shouldApply(type, mode, target))
                modified = pair.getFirst().apply(modified, pair.getSecond());
        }
        return modified;
    }

    public static class Mutable extends CraftingContext {

        private IMachineRecipe recipe;
        private ResourceLocation recipeId;

        public Mutable(MachineTile tile, IMachineUpgradeManager upgrades, int core) {
            super(tile, upgrades, null, () -> 0.0, core);
        }

        public Mutable setRecipe(IMachineRecipe recipe, ResourceLocation recipeId) {
            this.recipe = recipe;
            this.recipeId = recipeId;
            return this;
        }

        @Override
        public IMachineRecipe getRecipe() {
            return this.recipe;
        }

        @Override
        public ResourceLocation getRecipeId() {
            return this.recipeId;
        }
    }
}
