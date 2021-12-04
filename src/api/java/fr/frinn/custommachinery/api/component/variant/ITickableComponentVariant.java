package fr.frinn.custommachinery.api.component.variant;

import fr.frinn.custommachinery.api.component.IMachineComponentManager;

/**
 * Should be implemented by component variants that want to execute some code every tick, such as a slot that consume it's content.
 */
public interface ITickableComponentVariant {

    /**
     * Called every ticks on the server side by the component, or it's handler.
     * @param manager The machine tile's component manager.
     */
    void tick(IMachineComponentManager manager);
}
