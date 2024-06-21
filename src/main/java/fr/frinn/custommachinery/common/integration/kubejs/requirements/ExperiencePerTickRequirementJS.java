package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.ExperiencePerTickRequirement;
import fr.frinn.custommachinery.impl.integration.jei.Experience.Form;

public interface ExperiencePerTickRequirementJS extends RecipeJSBuilder {

  default RecipeJSBuilder requireXpPerTick(int xp) {
    return this.addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.INPUT, xp, Form.POINT));
  }

  default RecipeJSBuilder produceXpPerTick(int xp) {
    return this.addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.OUTPUT, xp, Form.POINT));
  }


  default RecipeJSBuilder requireLevelPerTick(int levels) {
    return this.addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.INPUT, levels, Form.LEVEL));
  }

  default RecipeJSBuilder produceLevelPerTick(int levels) {
    return this.addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.OUTPUT, levels, Form.LEVEL));
  }
}
