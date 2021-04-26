package fr.frinn.custommachinery.common.data.component;

import net.minecraft.nbt.CompoundNBT;

public interface IComponentSerializable {

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);
}
