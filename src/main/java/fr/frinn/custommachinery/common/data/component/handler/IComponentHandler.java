package fr.frinn.custommachinery.common.data.component.handler;

import fr.frinn.custommachinery.common.data.component.IMachineComponent;

import java.util.List;
import java.util.Optional;

public interface IComponentHandler<T extends IMachineComponent> extends IMachineComponent {

    List<T> getComponents();

    void putComponent(T component);

    Optional<T> getComponentForID(String id);
}
