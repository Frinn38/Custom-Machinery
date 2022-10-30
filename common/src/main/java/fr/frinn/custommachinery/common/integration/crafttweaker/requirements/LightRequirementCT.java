package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.LightRequirement;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

@ZenRegister
@Name(CTConstants.REQUIREMENT_LIGHT)
public interface LightRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireSkyLight(int level, @OptionalString(">=") String comparator) {
        return addRequirement(new LightRequirement(level, ComparatorMode.value(comparator), true));
    }

    @Method
    default T requireBlockLight(int level, @OptionalString(">=") String comparator) {
        return addRequirement(new LightRequirement(level, ComparatorMode.value(comparator), false));
    }
}
