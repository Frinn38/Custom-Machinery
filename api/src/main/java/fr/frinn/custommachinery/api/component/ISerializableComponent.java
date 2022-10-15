package fr.frinn.custommachinery.api.component;

import net.minecraft.nbt.CompoundTag;

/**
 * Used to store various data about the component inside the machine tile nbt tag.
 */
public interface ISerializableComponent extends IMachineComponent {

    /**
     * This Method is called by the fr.frinn.custommachinery.api.components.IMachineComponentManager when the CustomMachineTile data is stored to disk (mostly on chunk unload).
     * @param nbt The CompoundNBT that will be saved inside the machine tile nbt.
     */
    void serialize(CompoundTag nbt);

    /**
     * This Method is called by the IMachineComponentManager when the CustomMachineTile data is read from disk, directly after the components are created.
     * @param nbt The component CompoundNBT that was serialized in the above method.
     */
    void deserialize(CompoundTag nbt);
}
