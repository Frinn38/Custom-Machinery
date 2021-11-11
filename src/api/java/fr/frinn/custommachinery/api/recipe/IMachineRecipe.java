package fr.frinn.custommachinery.api.recipe;

import net.minecraft.util.ResourceLocation;

public interface IMachineRecipe {

    /**
     * @return The duration (in ticks) of the recipe.
     */
    int getRecipeTime();

    /**
     * @return The ID of the recipe (path to the json file, or the ID given by CT or Kubejs depending on how the recipe is created).
     */
    ResourceLocation getId();
}
