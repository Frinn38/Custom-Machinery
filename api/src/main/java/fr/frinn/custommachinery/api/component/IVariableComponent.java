package fr.frinn.custommachinery.api.component;

import fr.frinn.custommachinery.api.component.variant.IComponentVariant;

public interface IVariableComponent<T extends IComponentVariant> {

    T getVariant();
}
