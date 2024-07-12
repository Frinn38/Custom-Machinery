package fr.frinn.custommachinery.impl.crafting;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.crafting.IRecipeBuilder;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRecipeBuilder<T extends IMachineRecipe> implements IRecipeBuilder<T> {

    private final ResourceLocation machine;
    private List<RecipeRequirement<?, ?>> requirements = new ArrayList<>();
    private List<RecipeRequirement<?, ?>> jeiRequirements = new ArrayList<>();
    private int priority = 0;
    private int jeiPriority = 0;
    private boolean hidden = false;

    public AbstractRecipeBuilder(ResourceLocation machine) {
        this.machine = machine;
    }

    public AbstractRecipeBuilder(T recipe) {
        this(recipe.getMachineId());
        this.requirements = recipe.getRequirements();
        this.jeiRequirements = recipe.getJeiRequirements();
        this.priority = recipe.getPriority();
        this.jeiPriority = recipe.getJeiPriority();
        this.hidden = !recipe.showInJei();
    }

    public ResourceLocation getMachine() {
        return this.machine;
    }

    @Override
    public AbstractRecipeBuilder<T> withRequirement(RecipeRequirement<?, ?> requirement) {
        this.requirements.add(requirement);
        return this;
    }

    public List<RecipeRequirement<?, ?>> getRequirements() {
        return this.requirements;
    }

    @Override
    public AbstractRecipeBuilder<T> withJeiRequirement(RecipeRequirement<?, ?> requirement) {
        this.jeiRequirements.add(requirement);
        return this;
    }

    public List<RecipeRequirement<?, ?>> getJeiRequirements() {
        return this.jeiRequirements;
    }

    @Override
    public AbstractRecipeBuilder<T> withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public int getPriority() {
        return this.priority;
    }

    @Override
    public AbstractRecipeBuilder<T> withJeiPriority(int jeiPriority) {
        this.jeiPriority = jeiPriority;
        return this;
    }

    public int getJeiPriority() {
        return this.jeiPriority;
    }

    @Override
    public AbstractRecipeBuilder<T> hide() {
        this.hidden = true;
        return this;
    }

    public boolean isHidden() {
        return this.hidden;
    }
}
