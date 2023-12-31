package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.ExperiencePerTickRequirement;
import fr.frinn.custommachinery.common.requirement.ExperienceRequirement;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CTConstants.REQUIREMENT_EXPERIENCE_PER_TICK)
public interface ExperiencePerTickRequirementCT<T> extends RecipeCTBuilder<T> {

  @ZenCodeType.Method
  default T requireXpPerTick(float xp) {
    return addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.INPUT, xp));
  }

  @ZenCodeType.Method
  default T produceXpPerTick(float xp) {
    return addRequirement(new ExperiencePerTickRequirement(RequirementIOMode.OUTPUT, xp));
  }
}
