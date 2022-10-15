package fr.frinn.custommachinery.api.guielement;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;

import java.util.Optional;

public interface IComponentGuiElement<T extends IMachineComponent> {

    MachineComponentType<T> getComponentType();

    String getID();

    @SuppressWarnings("unchecked")
    default Optional<T> getComponent(IMachineComponentManager manager) {
        return manager.getComponent(getComponentType()).flatMap(component -> {
            if(component instanceof IComponentHandler handler)
                return handler.getComponentForID(getID());
            return Optional.of(component);
        });
    }
}
