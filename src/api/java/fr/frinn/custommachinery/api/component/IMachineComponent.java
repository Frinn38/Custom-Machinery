package fr.frinn.custommachinery.api.component;

/**
 * Implements this interface to define a Custom Machine component.
 * Machine Components are used to communicate some information about the machine status to their corresponding recipe requirement.
 * They can also be used to store values in the machine tile nbt, execute code each tick, expose capabilities,
 * modify the machine redstone comparator behaviour and provide additional information to The One Probe by implementing the corresponding sub-interface.
 * The machine component will be created by the MachineComponentManager when the machine tile is created.
 */
public interface IMachineComponent {

    /**
     * @return The MachineComponentType of this machine component, this must be the singleton object registered in the ForgeRegistry.
     */
    MachineComponentType<?> getType();

    /**
     * @return The ComponentIOMode of this component.
     */
    ComponentIOMode getMode();

    /**
     * @return The IMachineComponentManager that hold this IMachineComponent.
     * You can use that to access the CustomMachineTile directly or other components.
     */
    IMachineComponentManager getManager();
}
