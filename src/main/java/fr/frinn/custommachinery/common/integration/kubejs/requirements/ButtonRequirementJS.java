package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.ButtonRequirement;

import java.util.List;

public interface ButtonRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireButtonPressed(List<String> ids) {
        return addRequirement(new ButtonRequirement(ids, false));
    }

    default RecipeJSBuilder requireButtonReleased(List<String> ids) {
        return addRequirement(new ButtonRequirement(ids, true));
    }
}
