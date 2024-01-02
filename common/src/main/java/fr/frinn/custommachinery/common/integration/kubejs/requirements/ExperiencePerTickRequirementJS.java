package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.ExperiencePerTickRequirement;
import fr.frinn.custommachinery.impl.integration.jei.Experience.Form;

public interface ExperiencePerTickRequirementJS extends RecipeJSBuilder {

  default RecipeJSBuilder requireXpPerTick(float xp) {
    return this.addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.INPUT, xp, Form.POINT));
  }

  default RecipeJSBuilder produceXpPerTick(float xp) {
    return this.addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.OUTPUT, xp, Form.POINT));
  }


  default RecipeJSBuilder requireLevelPerTick(float xp) {
    return this.addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.INPUT, xp, Form.LEVEL));
  }

  default RecipeJSBuilder produceLevelPerTick(float xp) {
    return this.addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.OUTPUT, xp, Form.LEVEL));
  }
}
