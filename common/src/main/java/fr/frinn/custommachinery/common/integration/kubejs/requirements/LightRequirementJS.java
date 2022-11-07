package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.LightRequirement;
import fr.frinn.custommachinery.common.util.ComparatorMode;

public interface LightRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireSkyLight(int amount) {
        return this.requireSkyLight(amount, ">=");
    }

    default RecipeJSBuilder requireSkyLight(int amount, String comparator) {
        try {
            return this.addRequirement(new LightRequirement(amount, ComparatorMode.value(comparator), true));
        } catch (IllegalArgumentException e) {
            return error("Invalid comparator: {}", comparator);
        }
    }

    default RecipeJSBuilder requireBlockLight(int amount) {
        return this.requireBlockLight(amount, ">=");
    }

    default RecipeJSBuilder requireBlockLight(int amount, String comparator) {
        try {
            return this.addRequirement(new LightRequirement(amount, ComparatorMode.value(comparator), false));
        } catch (IllegalArgumentException e) {
            return error("Invalid comparator: {}", comparator);
        }
    }
}
