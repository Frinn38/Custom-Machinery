package fr.frinn.custommachinery.api.component;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * Used to store various data about the component inside the machine tile nbt tag.
 */
public interface ISerializableComponent extends IMachineComponent {

    /**
     * This Method is called by the {@link fr.frinn.custommachinery.api.component.IMachineComponentManager} when the CustomMachineTile data is stored to disk (mostly on chunk unload).
     * @param nbt The CompoundNBT that will be saved inside the machine tile nbt.
     * @param registries A lookup for registry based values.
     */
    void serialize(CompoundTag nbt, HolderLookup.Provider registries);

    /**
     * This Method is called by the {@link fr.frinn.custommachinery.api.component.IMachineComponentManager} when the {@link fr.frinn.custommachinery.api.machine.MachineTile} data is read from disk, directly after the components are created.
     * @param nbt The component CompoundNBT that was serialized in the above method.
     * @param registries A lookup for registry based values.
     */
    void deserialize(CompoundTag nbt, HolderLookup.Provider registries);
}
