package fr.frinn.custommachinery.common.data.component.handler;

import fr.frinn.custommachinery.common.data.component.IMachineComponent;

import java.util.List;

public interface IComponentHandler<T extends IMachineComponent> extends IMachineComponent {

    List<T> getComponents();

    void putComponent(T component);
}
