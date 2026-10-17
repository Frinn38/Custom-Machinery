package fr.frinn.custommachinery.api.component.handler;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.machine.MachineTile;

import java.util.List;
import java.util.Optional;

/**
 * An {@link IComponentHandler} is used to allow a {@link MachineTile} to hold several {@link IMachineComponent} of the same type (several item slots or fluid tank).
 * When a {@link IMachineComponent} whose type return false to {@link MachineComponentType#isSingle()} is passed to the {@link IMachineComponentManager},
 * the manager will put it into the existing {@link IComponentHandler} of the same type, or create one if it's the first {@link IMachineComponent} of this type.
 * The {@link IMachineComponentManager} hold only the {@link IComponentHandler} instance, which hold all {@link IMachineComponent} instances for its type.
 * A handler MUST NOT handle ticking, serializing, deserializing or syncing for its subcomponents, this is done automatically by the {@link IMachineComponentManager}.
 * @param <T> The {@link IMachineComponent} handled by this IComponentHandler.
 */
public interface IComponentHandler<T extends IMachineComponent> extends IMachineComponent {

    /**
     * @return The list of the {@link IMachineComponent} hold by this handler, This should be an immutable list or at least a copy of the original list.
     */
    List<T> getComponents();

    /**
     * Used to get a {@link IMachineComponent} by its String id.
     * @param id The unique id of the component to find.
     * @return An Optional component.
     */
    default Optional<T> getComponentForID(String id) {
        return this.getComponents().stream().filter(component -> component.getId().uniqueId().equals(id)).findFirst();
    }
}
