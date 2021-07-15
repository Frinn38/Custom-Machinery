package fr.frinn.custommachinery.api.components.handler;

import fr.frinn.custommachinery.api.components.IMachineComponent;

import java.util.List;
import java.util.Optional;

/**
 * An IComponentHandler is used to allow a MachineTile to hold several components of the same type (several item slots or fluid tank).
 * When a IMachineComponent whose type return false to MachineComponentType#isSingle is passed to the IMachineComponentManager,
 * the manager will put it into the existing IComponentHandler of the same type, or create one if it's the first component of this type.
 * The IMachineComponentManager hold only the IComponentHandler instance, which hold all IMachineComponent instances for it's type.
 * @param <T> The IMachineComponent handled by this IComponentHandler.
 */
public interface IComponentHandler<T extends IMachineComponent> extends IMachineComponent {

    /**
     * @return The list of the IMachineComponent hold by this handler, This should be an ImmutableList or at least a copy of the original list.
     */
    List<T> getComponents();

    /**
     * Used by the IMachineComponentManager to add a component in this handler.
     * @param component The IMachineComponent that should be handled.
     */
    void putComponent(T component);

    /**
     * Used to get a component by it's String id.
     * @param id The id of the component to find.
     * @return An Optional component.
     */
    Optional<T> getComponentForID(String id);
}
