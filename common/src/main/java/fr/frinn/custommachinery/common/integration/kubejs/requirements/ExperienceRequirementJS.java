package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.ExperienceRequirement;
import fr.frinn.custommachinery.impl.integration.jei.Experience.Form;

public interface ExperienceRequirementJS extends RecipeJSBuilder {
  default RecipeJSBuilder requireXp(float xp) {
    return this.addRequirement(new ExperienceRequirement(RequirementIOMode.INPUT, xp, Form.POINT));
  }

  default RecipeJSBuilder produceXp(float xp) {
    return this.addRequirement(new ExperienceRequirement(RequirementIOMode.OUTPUT, xp, Form.POINT));
  }

  default RecipeJSBuilder requireLevel(float xp) {
    return this.addRequirement(new ExperienceRequirement(RequirementIOMode.INPUT, xp, Form.LEVEL));
  }

  default RecipeJSBuilder produceLevel(float xp) {
    return this.addRequirement(new ExperienceRequirement(RequirementIOMode.OUTPUT, xp, Form.LEVEL));
  }
}
