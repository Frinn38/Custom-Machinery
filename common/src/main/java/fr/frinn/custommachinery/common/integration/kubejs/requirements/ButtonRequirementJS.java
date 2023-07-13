package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.ButtonRequirement;

public interface ButtonRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireButtonPressed(String id) {
        return addRequirement(new ButtonRequirement(id, true));
    }

    default RecipeJSBuilder requireButtonReleased(String id) {
        return addRequirement(new ButtonRequirement(id, false));
    }
}
