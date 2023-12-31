package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.ExperienceRequirement;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@Name(CTConstants.REQUIREMENT_EXPERIENCE)
public interface ExperienceRequirementCT<T> extends RecipeCTBuilder<T> {

  @Method
  default T requireXp(float xp) {
    return addRequirement(new ExperienceRequirement(RequirementIOMode.INPUT, xp));
  }

  @Method
  default T produceXp(float xp) {
    return addRequirement(new ExperienceRequirement(RequirementIOMode.OUTPUT, xp));
  }
}
