package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.requirement.RecipeRequirement;

public interface IRecipeBuilder<T extends IMachineRecipe> {

    IRecipeBuilder<T> withRequirement(RecipeRequirement<?, ?> requirement);

    IRecipeBuilder<T> withJeiRequirement(RecipeRequirement<?, ?> requirement);

    IRecipeBuilder<T> withPriority(int priority);

    IRecipeBuilder<T> withJeiPriority(int jeiPriority);

    IRecipeBuilder<T> hide();

    T build();
}
