package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.requirement.IRequirement;
import net.minecraft.resources.ResourceLocation;

public interface IRecipeBuilder<T extends IMachineRecipe> {

    IRecipeBuilder<T> withRequirement(IRequirement<?> requirement);

    IRecipeBuilder<T> withJeiRequirement(IRequirement<?> requirement);

    IRecipeBuilder<T> withPriority(int priority);

    IRecipeBuilder<T> withJeiPriority(int jeiPriority);

    T build(ResourceLocation id);
}
