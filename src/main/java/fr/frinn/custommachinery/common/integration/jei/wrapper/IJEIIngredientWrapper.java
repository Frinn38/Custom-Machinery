package fr.frinn.custommachinery.common.integration.jei.wrapper;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;

import java.util.List;

public interface IJEIIngredientWrapper<T> {

    IIngredientType<T> getJEIIngredientType();

    Object asJEIIngredient();

    List<T> getJeiIngredients();
}
