package fr.frinn.custommachinery.api.component;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Used to store various data about the component inside the machine tile nbt tag.
 */
public interface ISerializableComponent extends IMachineComponent {

    /**
     * This Method is called by the {@link fr.frinn.custommachinery.api.component.IMachineComponentManager} when the CustomMachineTile data is stored to disk (mostly on chunk unload).
     * @param output An abstraction of NBT system, which takes all data that needs to be serialized.
     */
    void serialize(ValueOutput output);

    /**
     * This Method is called by the {@link fr.frinn.custommachinery.api.component.IMachineComponentManager} when the {@link fr.frinn.custommachinery.api.machine.MachineTile} data is read from disk, directly after the components are created.
     * @param input An abstraction of NBT system, which gives all data that were serialized.
     */
    void deserialize(ValueInput input);
}
