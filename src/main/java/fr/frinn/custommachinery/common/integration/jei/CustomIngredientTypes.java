package fr.frinn.custommachinery.common.integration.jei;

import fr.frinn.custommachinery.common.integration.jei.energy.Energy;
import mezz.jei.api.ingredients.IIngredientType;

public class CustomIngredientTypes {

    public static final IIngredientType<Energy> ENERGY = () -> Energy.class;
}
