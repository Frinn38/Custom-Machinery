package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.ExperienceRequirement;

public interface ExperienceRequirementJS extends RecipeJSBuilder {

  default RecipeJSBuilder requireXp(float xp) {
    return this.addRequirement(new ExperienceRequirement(RequirementIOMode.INPUT, xp));
  }

  default RecipeJSBuilder produceXp(float xp) {
    return this.addRequirement(new ExperienceRequirement(RequirementIOMode.OUTPUT, xp));
  }
}
