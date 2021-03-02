package fr.frinn.custommachinery.common.data.component;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICapabilityMachineComponent {

    <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side);

    void invalidateCapability();
}
