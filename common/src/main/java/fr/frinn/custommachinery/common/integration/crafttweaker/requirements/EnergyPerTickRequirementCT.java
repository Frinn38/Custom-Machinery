package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.EnergyPerTickRequirement;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name(CTConstants.REQUIREMENT_ENERGY_PER_TICK)
public interface EnergyPerTickRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireEnergyPerTick(int amount) {
        return addRequirement(new EnergyPerTickRequirement(RequirementIOMode.INPUT, amount));
    }

    @Method
    default T produceEnergyPerTick(int amount) {
        return addRequirement(new EnergyPerTickRequirement(RequirementIOMode.OUTPUT, amount));
    }
}
