package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.FuelRequirement;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalInt;

@ZenRegister
@Name(CTConstants.REQUIREMENT_FUEL)
public interface FuelRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireFuel(@OptionalInt(1) int amount) {
        return addRequirement(new FuelRequirement(amount));
    }
}
