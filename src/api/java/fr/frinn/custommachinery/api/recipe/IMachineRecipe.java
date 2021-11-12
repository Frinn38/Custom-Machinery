package fr.frinn.custommachinery.api.recipe;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;

public interface IMachineRecipe extends IRecipe<IInventory> {

    /**
     * @return The duration (in ticks) of the recipe.
     */
    int getRecipeTime();
}
