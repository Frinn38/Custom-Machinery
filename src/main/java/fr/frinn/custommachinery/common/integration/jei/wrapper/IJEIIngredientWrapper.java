package fr.frinn.custommachinery.common.integration.jei.wrapper;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;

public interface IJEIIngredientWrapper<T> {

    IIngredientType<T> getJEIIngredientType();

    Object asJEIIngredient();

    void addJeiIngredients(IIngredients ingredients);
}
