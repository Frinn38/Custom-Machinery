package fr.frinn.custommachinery.api.component;

import fr.frinn.custommachinery.api.machine.MachineTile;
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
     */
    default void setLevel(Level level) {}

    /**
     * Called on the server side when the {@link MachineTile} is removed from the level, either on chunk unload or when the block is broken.
     */
    default void onRemoved() {}
}
