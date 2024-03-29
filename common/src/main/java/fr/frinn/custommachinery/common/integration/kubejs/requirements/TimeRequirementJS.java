package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.TimeRequirement;
import fr.frinn.custommachinery.impl.util.IntRange;

public interface TimeRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireTime(String time) {
        try {
            IntRange range = IntRange.createFromString(time);
            return this.addRequirement(new TimeRequirement(range));
        } catch (IllegalArgumentException e) {
            return error("Impossible to parse time range: \"{}\", ", time, e);
        }
    }
}
