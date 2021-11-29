package fr.frinn.custommachinery.api.integration.jei;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;

/**
 * This class link one recipe requirement to a JEI ingredient.
 * A wrapper is needed to avoid to load jei class directly inside the requirement, crashing the game when JEI is not present.
 * @param <T> The type of ingredient this wrapper support (ItemStack and FluidStack are vanilla ingredient supported by jei, but mods can add their own).
 */
public interface IJEIIngredientWrapper<T> {

    /**
     * The type of ingredient supported by the wrapper.
     * Use the constants defined in VanillaTypes (JEI class) or CustomIngredientTypes (CM class).
     * @return The IIngredientType corresponding to this wrapper supported ingredient.
     */
    IIngredientType<T> getJEIIngredientType();

    /**
     * Called by the JEI integration to know which ingredients this requirement will require or produce.
     * The wrapper should take care only of its requirement, other requirements should have their own wrapper.
     * @param ingredients Use this to specify all inputs and outputs required or produced by this requirement.
     */
    void setIngredient(IIngredients ingredients);

    /**
     * Called by the JEI integration to gather all ingredients that will be displayed in a certain GuiElement of the recipe layout (the rectangle around the recipe in the JEI gui).
     * If true is returned by this method, the JEI integration will consider that this element found an ingredient and won't try to fit any other ingredient in this element.
     * @param index The index of this ingredient, use it when calling init or set on the layout.
     * @param layout The jei recipe layout, handle all ingredients and display them on the screen.
     * @param xOffset The X axis (horizontal) offset from the left of the screen of the recipe layout.
     * @param yOffset The Y axis (vertical) offset from the top of the screen of the recipe layout.
     * @param element The gui element that will be rendered at this place.
     * @param renderer The ingredient renderer, pass it to the layout to make it render the ingredient on the recipe gui.
     * @param helper A helper class, use it to retrieve a IMachineComponentTemplate from the passed GuiElement.
     *               The component template is needed to know whenever this element can accept the ingredient (slots can have a filter).
     * @return True if the wrapper successfully handled this element, false otherwise.
     */
    boolean setupRecipe(int index, IRecipeLayout layout, int xOffset, int yOffset, IGuiElement element, IIngredientRenderer<T> renderer, IRecipeHelper helper);
}
