package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.ExperiencePerTickRequirement;

public interface ExperiencePerTickRequirementJS extends RecipeJSBuilder {

  default RecipeJSBuilder requireXpPerTick(float xp) {
    return this.addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.INPUT, xp));
  }

  default RecipeJSBuilder produceXpPerTick(float xp) {
    return this.addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.OUTPUT, xp));
  }
}
