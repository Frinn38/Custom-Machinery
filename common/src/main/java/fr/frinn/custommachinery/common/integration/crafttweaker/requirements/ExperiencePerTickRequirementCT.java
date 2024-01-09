package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.ExperiencePerTickRequirement;
import fr.frinn.custommachinery.impl.integration.jei.Experience.Form;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@Name(CTConstants.REQUIREMENT_EXPERIENCE_PER_TICK)
public interface ExperiencePerTickRequirementCT<T> extends RecipeCTBuilder<T> {

  @Method
  default T requireXpPerTick(int xp) {
    return addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.INPUT, xp, Form.POINT));
  }

  @Method
  default T produceXpPerTick(int xp) {
    return addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.OUTPUT, xp, Form.POINT));
  }


  @Method
  default T requireLevelPerTick(int levels) {
    return addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.INPUT, levels, Form.LEVEL));
  }

  @Method
  default T produceLevelPerTick(int levels) {
    return addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.OUTPUT, levels, Form.LEVEL));
  }
}
