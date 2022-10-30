package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.RedstoneRequirement;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

@ZenRegister
@Name(CTConstants.REQUIREMENT_REDSTONE)
public interface RedstoneRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireRedstone(int power, @OptionalString(">=") String comparator) {
        return addRequirement(new RedstoneRequirement(power, ComparatorMode.value(comparator)));
    }
}
