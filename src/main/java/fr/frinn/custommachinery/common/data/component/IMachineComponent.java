package fr.frinn.custommachinery.common.data.component;

import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IMachineComponent {

    MachineComponentType getType();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);

    void addProbeInfo(IProbeInfo info);
}
