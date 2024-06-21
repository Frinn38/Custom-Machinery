package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.PositionRequirement;
import fr.frinn.custommachinery.impl.util.IntRange;

public interface PositionRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requirePosition(String xString, String yString, String zString) {
        IntRange x;
        IntRange y;
        IntRange z;
        try {
            x = IntRange.createFromString(xString);
        } catch (IllegalArgumentException e) {
            return error("Invalid X position range: {} {}", xString, e.getMessage());
        }
        try {
            y = IntRange.createFromString(yString);
        } catch (IllegalArgumentException e) {
            return error("Invalid Y position range: {} {}", xString, e.getMessage());
        }
        try {
            z = IntRange.createFromString(zString);
        } catch (IllegalArgumentException e) {
            return error("Invalid Z position range: {} {}", xString, e.getMessage());
        }
        return addRequirement(new PositionRequirement(x, y, z));
    }
}
