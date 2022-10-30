package fr.frinn.custommachinery.common.integration.crafttweaker;

import fr.frinn.custommachinery.api.requirement.IRequirement;

public interface RecipeCTBuilder<T> {

    T addRequirement(IRequirement<?> requirement);

    T error(String error, Object... args);
}
