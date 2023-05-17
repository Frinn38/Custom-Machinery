package fr.frinn.custommachinery.api.component;

import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * Implements this interface to define a custom machine component.
 * Machine components are used to communicate some information about the machine status to their corresponding recipe requirement.
 * They can also be used to store values in the machine tile nbt, execute code each tick, expose capabilities,
 * and modify the machine redstone comparator behaviour by implementing the corresponding sub-interfaces.
 * The machine component will be created by the {@link IMachineComponentManager} when the {@link MachineTile} is created.
 */
public interface IMachineComponent {

    /**
     * @return The {@link MachineComponentType} of this machine component, this must be the singleton registered object.
     */
    MachineComponentType<?> getType();

    /**
     * @return The {@link ComponentIOMode} of this {@link IMachineComponent}.
     */
    ComponentIOMode getMode();

    /**
     * @return The {@link IMachineComponentManager} that hold this {@link IMachineComponent}.
     * You can use that to access the {@link MachineTile} directly or other components.
     */
    IMachineComponentManager getManager();

    /**
     * Called on both sides when the {@link Level} is set on the {@link MachineTile}.
     * Use this method to initialize the component for things that depends on the {@link Level} of the {@link MachineTile}.
     */
    default void init() {}

    /**
     * Called on the server side when the {@link MachineTile} is removed from the level, either on chunk unload or when the block is broken.
     */
    default void onRemoved() {}

    /**
     * Called on both sides when the status of the {@link MachineTile} changed.
     * @param oldStatus The {@link MachineStatus} of the {@link MachineTile} before the change.
     * @param newStatus The {@link MachineStatus} of the {@link MachineTile} after the change.
     * @param message The error message displayed in the GUI (and TOP and WAILA) when the machine status is "ERROR", empty otherwise.
     */
    default void onStatusChanged(MachineStatus oldStatus, MachineStatus newStatus, Component message) {}
}
