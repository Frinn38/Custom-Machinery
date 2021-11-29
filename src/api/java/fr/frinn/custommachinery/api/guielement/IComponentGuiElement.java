package fr.frinn.custommachinery.api.guielement;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;

public interface IComponentGuiElement<T extends IMachineComponent> {

    MachineComponentType<T> getComponentType();

    String getID();
}
