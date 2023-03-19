package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.PositionRequirement;
import fr.frinn.custommachinery.impl.util.IntRange;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name(CTConstants.REQUIREMENT_POSITION)
public interface PositionRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requirePosition(String xString, String yString, String zString) {
        IntRange x;
        IntRange y;
        IntRange z;
        try {
            x = IntRange.createFromString(xString);
        } catch (IllegalArgumentException e) {
            return error("Invalid X position range: {} {}", xString, e.getMessage());
        }
        try {
            y = IntRange.createFromString(yString);
        } catch (IllegalArgumentException e) {
            return error("Invalid Y position range: {} {}", xString, e.getMessage());
        }
        try {
            z = IntRange.createFromString(zString);
        } catch (IllegalArgumentException e) {
            return error("Invalid Z position range: {} {}", xString, e.getMessage());
        }
        return addRequirement(new PositionRequirement(x, y, z));
    }
}
