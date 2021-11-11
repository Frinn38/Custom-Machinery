package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.RequirementType;
import fr.frinn.custommachinery.common.data.upgrade.RecipeModifier;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

public class CraftingContext {

    private static final Random RAND = new Random();

    private final CustomMachineTile tile;
    private CustomMachineRecipe recipe;
    private double recipeProgressTime = 0;
    private Map<RecipeModifier, Integer> modifiers = new HashMap<>();

    public CraftingContext(CustomMachineTile tile) {
        this.tile = tile;
        this.refreshModifiers(tile);
    }

    public CustomMachineTile getTile() {
        return this.tile;
    }

    public void refreshModifiers(CustomMachineTile tile) {
        this.modifiers = Utils.getModifiersForTile(tile);
    }

    public void setRecipe(CustomMachineRecipe recipe) {
        this.recipe = recipe;
    }

    public CustomMachineRecipe getRecipe() {
        return this.recipe;
    }

    public void setRecipeProgressTime(double recipeProgressTime) {
        this.recipeProgressTime = recipeProgressTime;
    }

    public double getRecipeProgressTime() {
        return this.recipeProgressTime;
    }

    public double getRemainingTime() {
        if(this.recipe == null)
            return 0;
        return this.recipe.getRecipeTime() - this.recipeProgressTime;
    }

    public double getModifiedSpeed() {
        if(this.recipe == null)
            return 1;
        int baseTime = this.recipe.getRecipeTime();
        double modifiedTime = getModifiedValue(baseTime, Registration.SPEED_REQUIREMENT.get(), null, null);
        double speed = baseTime / modifiedTime;
        return Math.max(0.01, speed);
    }

    public Collection<RecipeModifier> getModifiers() {
        return this.modifiers.keySet();
    }

    public double getModifiedvalue(double value, IRequirement<?> requirement, @Nullable String target) {
        return getModifiedValue(value, requirement.getType(), target, requirement.getMode());
    }

    public double getPerTickModifiedValue(double value, IRequirement<?> requirement, @Nullable String target) {
        if(this.getRemainingTime() > 0)
            return getModifiedvalue(value, requirement, target) * Math.min(this.getModifiedSpeed(), this.getRemainingTime());
        return getModifiedvalue(value, requirement, target) * this.getModifiedSpeed();
    }

    public double getModifiedValue(double value, RequirementType<?> type, @Nullable String target, @Nullable IRequirement.MODE mode) {
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
                case ADDITION:
                    toAdd += modifier.getModifier();
                    break;
                case MULTIPLICATION:
                    toMult *= modifier.getModifier();
                    break;
            }
        }
        return (value + toAdd) * toMult;
    }
}
