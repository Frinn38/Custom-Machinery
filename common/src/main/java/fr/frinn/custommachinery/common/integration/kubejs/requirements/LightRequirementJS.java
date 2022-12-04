package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.LightRequirement;
import fr.frinn.custommachinery.impl.util.IntRange;

public interface LightRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireSkyLight(String level) {
        try {
            return this.addRequirement(new LightRequirement(IntRange.createFromString(level), true));
        } catch (IllegalArgumentException e) {
            return error("Invalid light level range: {}, {}", level, e.getMessage());
        }
    }

    default RecipeJSBuilder requireBlockLight(String level) {
        try {
            return this.addRequirement(new LightRequirement(IntRange.createFromString(level), false));
        } catch (IllegalArgumentException e) {
            return error("Invalid light level range: {}, {}", level, e.getMessage());
        }
    }
}
