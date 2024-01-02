package fr.frinn.custommachinery.impl.integration.jei;

import mezz.jei.api.ingredients.IIngredientType;

/**
 * Ingredient types added by CM.
 */
public class CustomIngredientTypes {

    /**
     * Used by the energy requirement to set the amount of energy needed by a recipe in JEI.
     */
    public static final IIngredientType<Energy> ENERGY = () -> Energy.class;
    public static final IIngredientType<Experience> EXPERIENCE = () -> Experience.class;
}
