package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.FuelRequirement;

public interface FuelRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireFuel() {
        return requireFuel(1);
    }

    default RecipeJSBuilder requireFuel(int amount) {
        return this.addRequirement(new FuelRequirement(amount));
    }
}
