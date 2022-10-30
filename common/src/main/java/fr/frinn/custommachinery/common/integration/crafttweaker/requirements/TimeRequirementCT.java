package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.TimeRequirement;
import fr.frinn.custommachinery.impl.util.IntRange;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name(CTConstants.REQUIREMENT_TIME)
public interface TimeRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireTime(String time) {
        try {
            IntRange range = IntRange.createFromString(time);
            return addRequirement(new TimeRequirement(range));
        } catch (IllegalArgumentException e) {
            return error("Impossible to parse time range : {},\n{}", time, e.getMessage());
        }
    }
}
