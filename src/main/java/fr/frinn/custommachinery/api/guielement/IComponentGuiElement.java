package fr.frinn.custommachinery.api.guielement;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;

import java.util.Optional;

/**
 * Must be implemented by any {@link IGuiElement} linked to a specific {@link IMachineComponent}, like a slot for an item component.
 * @param <T> The {@link IMachineComponent} linked to this {@link IGuiElement}.
 */
public interface IComponentGuiElement<T extends IMachineComponent> {

    /**
     * @return The {@link MachineComponentType} of the {@link IMachineComponent} linked to this {@link IGuiElement}.
     */
    MachineComponentType<T> getComponentType();

    /**
     * @return The id of the {@link IMachineComponent} linked to this {@link IGuiElement} in case it is not unique for its type.
     */
    String getComponentId();

    /**
     * Try to find an {@link IMachineComponent} linked to this {@link IGuiElement}.
     * @param manager The {@link IMachineComponentManager} which hold all the components for the {@link fr.frinn.custommachinery.api.machine.MachineTile} using this {@link IGuiElement}.
     * @return An {@link IMachineComponent} which match the type and id specified by this {@link IComponentGuiElement}, or empty.
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    default Optional<T> getComponent(IMachineComponentManager manager) {
        return manager.getComponent(getComponentType()).flatMap(component -> {
            if(component instanceof IComponentHandler handler)
                return handler.getComponentForID(getComponentId());
            return Optional.of(component);
        });
    }
}
