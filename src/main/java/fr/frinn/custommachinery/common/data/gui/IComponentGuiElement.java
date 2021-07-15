package fr.frinn.custommachinery.common.data.gui;

import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.api.components.MachineComponentType;

public interface IComponentGuiElement<T extends IMachineComponent> {

    MachineComponentType<T> getComponentType();

    String getID();
}
