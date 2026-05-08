package fr.frinn.custommachinery.client.integration.jei;

import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CustomMachineRecipeCategory extends AbstractRecipeCategory<CustomMachineRecipe, RecipeHolder<CustomMachineRecipe>> {

    public CustomMachineRecipeCategory(CustomMachine machine, RecipeType<RecipeHolder<CustomMachineRecipe>> type, IJeiHelpers helpers) {
        super(machine, type, helpers);
    }
}
