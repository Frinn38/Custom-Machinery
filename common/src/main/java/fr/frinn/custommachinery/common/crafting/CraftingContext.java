package fr.frinn.custommachinery.common.crafting;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.upgrade.IMachineUpgradeManager;
import fr.frinn.custommachinery.api.upgrade.IRecipeModifier;
import fr.frinn.custommachinery.common.init.Registration;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CraftingContext implements ICraftingContext {

    private final IProcessor manager;
    private final IMachineUpgradeManager upgrades;
    private final IMachineRecipe recipe;
    private final List<Pair<IRecipeModifier, Integer>> fixedModifiers;

    public CraftingContext(IProcessor manager, IMachineUpgradeManager upgrades, IMachineRecipe recipe) {
        this.manager = manager;
        this.upgrades = upgrades;
        this.recipe = recipe;
        this.fixedModifiers = Collections.unmodifiableList(upgrades.getAllModifiers());
    }

    @Override
    public MachineTile getMachineTile() {
        return this.manager.getTile();
    }

    @Override
    public IMachineRecipe getRecipe() {
        return this.recipe;
    }

    @Override
    public double getRemainingTime() {
        if(getRecipe() == null)
            return 0;
        return getRecipe().getRecipeTime() - this.manager.getRecipeProgressTime();
    }

    @Override
    public double getModifiedSpeed() {
        if(getRecipe() == null)
            return 1;
        int baseTime = getRecipe().getRecipeTime();
        double modifiedTime = getModifiedValue(baseTime, Registration.SPEED_REQUIREMENT.get(), null, null);
        double speed = baseTime / modifiedTime;
        return Math.max(0.01, speed);
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
        List<Pair<IRecipeModifier, Integer>> modifiers = type instanceof ITickableRequirement<?> ? this.upgrades.getAllModifiers() : this.fixedModifiers;
        for(Pair<IRecipeModifier, Integer> pair : modifiers) {
            if(pair.getFirst().shouldApply(type, mode, target))
                modified = pair.getFirst().apply(modified, pair.getSecond());
        }
        return modified;
    }

    public static class Mutable extends CraftingContext {

        private IMachineRecipe recipe;

        public Mutable(IProcessor manager, IMachineUpgradeManager upgrades) {
            super(manager, upgrades, null);
        }

        public Mutable setRecipe(IMachineRecipe recipe) {
            this.recipe = recipe;
            return this;
        }

        @Override
        public IMachineRecipe getRecipe() {
            return this.recipe;
        }
    }
}
