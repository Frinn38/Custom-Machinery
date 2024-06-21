package fr.frinn.custommachinery.client.integration.jei;

import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;

public class CustomMachineRecipeCategory extends AbstractRecipeCategory<CustomMachineRecipe> {

    public CustomMachineRecipeCategory(CustomMachine machine, RecipeType<CustomMachineRecipe> type, IJeiHelpers helpers) {
        super(machine, type, helpers);
    }
}
