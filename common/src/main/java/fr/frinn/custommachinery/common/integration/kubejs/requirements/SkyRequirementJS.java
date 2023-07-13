package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.SkyRequirement;

public interface SkyRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder mustSeeSky() {
        return this.addRequirement(new SkyRequirement());
    }
}
