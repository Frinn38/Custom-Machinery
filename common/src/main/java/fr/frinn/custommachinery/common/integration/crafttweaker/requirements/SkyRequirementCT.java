package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.SkyRequirement;
import org.openzen.zencode.java.ZenCodeType.Method;

public interface SkyRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T mustSeeSky() {
        return addRequirement(new SkyRequirement());
    }
}
