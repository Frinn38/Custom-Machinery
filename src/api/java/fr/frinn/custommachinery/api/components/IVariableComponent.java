package fr.frinn.custommachinery.api.components;

import fr.frinn.custommachinery.api.components.variant.IComponentVariant;

public interface IVariableComponent<T extends IComponentVariant> {

    T getVariant();
}
