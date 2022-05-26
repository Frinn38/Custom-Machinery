package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.data.upgrade.RecipeModifier;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

public class CraftingContext implements ICraftingContext {

    private static final Random RAND = new Random();

    private final CraftingManager manager;
    private final CustomMachineRecipe recipe;
    private Map<RecipeModifier, Integer> modifiers = new HashMap<>();
    private int modifiersCheckCooldown;

    public CraftingContext(CraftingManager manager, CustomMachineRecipe recipe) {
        this.manager = manager;
        this.recipe = recipe;
        this.modifiersCheckCooldown = Utils.RAND.nextInt(20);
        refreshModifiers();
    }

    @Override
    public CustomMachineTile getMachineTile() {
        return this.manager.getTile();
    }

    public void tickModifiers() {
        if(this.modifiersCheckCooldown-- <= 0) {
            this.refreshModifiers();
            this.modifiersCheckCooldown = 20;
        }
    }

    public void refreshModifiers() {
        this.modifiers = Utils.getModifiersForTile(manager.getTile());
    }

    @Override
    public CustomMachineRecipe getRecipe() {
        return this.recipe;
    }

    @Override
    public double getRemainingTime() {
        if(getRecipe() == null)
            return 0;
        return getRecipe().getRecipeTime() - this.manager.recipeProgressTime;
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

    public Collection<RecipeModifier> getModifiers() {
        return this.modifiers.keySet();
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
        List<RecipeModifier> toApply = new ArrayList<>();
        this.modifiers.entrySet().stream()
                .filter(entry -> type == null || entry.getKey().getRequirementType() == type)
                .filter(entry -> target == null || entry.getKey().getTarget().equals(target))
                .filter(entry -> mode == null || entry.getKey().getMode() == mode)
                .filter(entry -> entry.getKey().getChance() > RAND.nextDouble())
                .forEach(entry -> IntStream.range(0, entry.getValue()).forEach(index -> toApply.add(entry.getKey())));

        double toAdd = 0.0D;
        double toMult = 1.0D;
        for(RecipeModifier modifier : toApply) {
            switch (modifier.getOperation()) {
                case ADDITION -> toAdd += modifier.getModifier();
                case MULTIPLICATION -> toMult *= modifier.getModifier();
            }
        }
        return (value + toAdd) * toMult;
    }

    public static class Mutable extends CraftingContext {

        private CustomMachineRecipe recipe;

        public Mutable(CraftingManager manager) {
            super(manager, null);
        }

        public Mutable setRecipe(CustomMachineRecipe recipe) {
            this.recipe = recipe;
            return this;
        }

        @Override
        public CustomMachineRecipe getRecipe() {
            return this.recipe;
        }

        @Override
        public void refreshModifiers() {

        }
    }
}
