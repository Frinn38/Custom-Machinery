package fr.frinn.custommachinery.api.integration.jei;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;

/**
 * This class link one recipe requirement to a JEI ingredient.
 * A wrapper is needed to avoid to load jei class directly inside the requirement, crashing the game when JEI is not present.
 * @param <T> The type of ingredient this wrapper support (ItemStack and FluidStack are vanilla ingredient supported by jei, but mods can add their own).
 */
public interface IJEIIngredientWrapper<T> {

    /**
     * Called by the JEI integration to gather all ingredients that will be displayed in a certain GuiElement of the recipe layout (the rectangle around the recipe in the JEI gui).
     * If true is returned by this method, the JEI integration will consider that this element found an ingredient and won't try to fit any other ingredient in this element.
     *
     * @param layout  The jei recipe layout, handle all ingredients and display them on the screen.
     * @param xOffset The X axis (horizontal) offset from the left of the screen of the recipe layout.
     * @param yOffset The Y axis (vertical) offset from the top of the screen of the recipe layout.
     * @param element The gui element that will be rendered at this place.
     * @param helper  A helper class, use it to retrieve a IMachineComponentTemplate from the passed GuiElement.
     *                The component template is needed to know whenever this element can accept the ingredient (slots can have a filter).
     * @return True if the wrapper successfully handled this element, false otherwise.
     */
    boolean setupRecipe(IRecipeLayoutBuilder layout, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper);

    /**
     * Utility method to get a recipe ingredient role from a recipe requirement mode.
     * @param mode The mode of the requirement.
     * @return The role (input or output) of this ingredient.
     */
    default RecipeIngredientRole roleFromMode(RequirementIOMode mode) {
        return mode == RequirementIOMode.INPUT ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
    }
}
