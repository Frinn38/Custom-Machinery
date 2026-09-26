package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.init.CMRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.function.Supplier;

public class CraftingContext implements ICraftingContext {

    private final MachineTile tile;
    private final RecipeHolder<? extends IMachineRecipe> recipe;
    private final Supplier<Double> progressTimeGetter;
    private final int core;
    private double baseSpeed = 1.0D;

    public CraftingContext(MachineTile tile, RecipeHolder<? extends IMachineRecipe> recipe, Supplier<Double> progressTimeGetter, int core) {
        this.tile = tile;
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
    public Identifier getRecipeId() {
        return this.recipe.id().identifier();
    }

    @Override
    public double getRemainingTime() {
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
        int baseTime = getRecipe().getRecipeTime();
        double modifiedTime = getModifiedValue(baseTime, CMRegistration.SPEED_REQUIREMENT.get(), null, RequirementIOMode.INPUT);
        double speed = baseTime * this.baseSpeed / modifiedTime;
        return Math.max(0.01, speed);
    }
}
