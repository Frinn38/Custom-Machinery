package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.ChunkloadRequirement;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalInt;

@ZenRegister
@Name(CTConstants.REQUIREMENT_CHUNKLOAD)
public interface ChunkloadRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T chunkload(@OptionalInt(1) int radius) {
        if(radius < 1 || radius > 32)
            return error("Invalid radius for chunkload requirement: {}.\nMust be between 1 and 32", radius);
        return addRequirement(new ChunkloadRequirement(radius));
    }
}
