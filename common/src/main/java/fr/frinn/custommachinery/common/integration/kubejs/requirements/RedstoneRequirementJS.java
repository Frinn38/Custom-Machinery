package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.RedstoneRequirement;
import fr.frinn.custommachinery.common.util.ComparatorMode;

public interface RedstoneRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireRedstone(int power) {
        return this.requireRedstone(power, ">=");
    }

    default RecipeJSBuilder requireRedstone(int power, String comparator) {
        try {
            return this.addRequirement(new RedstoneRequirement(power, ComparatorMode.value(comparator)));
        } catch (IllegalArgumentException e) {
            return error("Invalid comparator: {}", comparator);
        }
    }
}
