package fr.frinn.custommachinery.common.integration.jei;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;

public interface IJEIIngredientRequirement {

    IIngredientType<?> getJEIIngredientType();

    Object asJEIIngredient();

    void addJeiIngredients(IIngredients ingredients);
}
