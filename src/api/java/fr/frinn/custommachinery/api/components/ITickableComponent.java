package fr.frinn.custommachinery.api.components;

/**
 * Used to do some logic each tick of the MachineTile.
 */
public interface ITickableComponent extends IMachineComponent {

    /**
     * Called each tick for each component that implements this interface, for each MachineTile loaded in the world.
     */
    void tick();
}
