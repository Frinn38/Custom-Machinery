package fr.frinn.custommachinery.common.data.component;

import net.minecraft.nbt.CompoundNBT;

public interface IMachineComponent {

    MachineComponentType getType();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);
}
