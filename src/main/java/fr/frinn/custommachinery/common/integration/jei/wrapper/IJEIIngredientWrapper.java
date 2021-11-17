package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.common.integration.jei.RecipeHelper;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;

public interface IJEIIngredientWrapper<T> {

    IIngredientType<T> getJEIIngredientType();

    void setIngredient(IIngredients ingredients);

    boolean setupRecipe(int index, IRecipeLayout layout, IGuiElement element, RecipeHelper helper);
}
