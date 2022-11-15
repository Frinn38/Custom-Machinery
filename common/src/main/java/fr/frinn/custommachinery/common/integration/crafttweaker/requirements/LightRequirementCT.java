package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.LightRequirement;
import fr.frinn.custommachinery.impl.util.IntRange;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name(CTConstants.REQUIREMENT_LIGHT)
public interface LightRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireSkyLight(int level) {
        return requireSkyLight("" + level);
    }

    @Method
    default T requireSkyLight(String level) {
        try {
            return addRequirement(new LightRequirement(IntRange.createFromString(level), true));
        } catch (IllegalArgumentException e) {
            return error("Invalid light level range: {}, {}", level, e.getMessage());
        }
    }

    @Method
    default T requireBlockLight(int level) {
        return requireBlockLight("" + level);
    }

    @Method
    default T requireBlockLight(String level) {
        try {
            return addRequirement(new LightRequirement(IntRange.createFromString(level), false));
        } catch (IllegalArgumentException e) {
            return error("Invalid light level range: {}, {}", level, e.getMessage());
        }
    }
}
