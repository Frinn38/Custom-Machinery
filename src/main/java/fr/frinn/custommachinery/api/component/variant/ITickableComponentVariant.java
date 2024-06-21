package fr.frinn.custommachinery.api.component.variant;

import fr.frinn.custommachinery.api.component.IMachineComponent;

/**
 * Should be implemented by component variants that want to execute some code every tick, such as a slot that consume it's content.
 */
public interface ITickableComponentVariant<T extends IMachineComponent> {

    /**
     * Called every ticks on the server side by the component, or it's handler.
     * @param component The component with this variant.
     */
    void tick(T component);
}
