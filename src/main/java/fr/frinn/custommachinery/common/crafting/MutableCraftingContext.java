package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public class MutableCraftingContext implements ICraftingContext {

    private final MachineTile tile;
    private final int core;
    @Nullable
    private RecipeHolder<? extends IMachineRecipe> recipe;
    private double baseSpeed = 1.0D;

    public MutableCraftingContext(MachineTile tile, int core) {
        this.tile = tile;
        this.recipe = null;
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
        if(this.recipe == null)
            throw new IllegalStateException("Trying to get null recipe on mutable crafting context");
        return this.recipe.value();
    }

    public MutableCraftingContext setRecipe(@Nullable RecipeHolder<? extends IMachineRecipe> recipe) {
        this.recipe = recipe;
        return this;
    }

    @Override
    public ResourceLocation getRecipeId() {
        if(this.recipe == null)
            throw new IllegalStateException("Trying to get null recipe on mutable crafting context");
        return this.recipe.id();
    }

    @Override
    public double getRemainingTime() {
        return getRecipe().getRecipeTime();
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
        double modifiedTime = getModifiedValue(baseTime, Registration.SPEED_REQUIREMENT.get(), null, RequirementIOMode.INPUT);
        double speed = baseTime * this.baseSpeed / modifiedTime;
        return Math.max(0.01, speed);
    }
}
