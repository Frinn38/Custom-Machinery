package fr.frinn.custommachinery.api.component;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used to expose one or several Capability data.
 * When something call getCapability on the custom machine tile,
 * the tile will try to get the corresponding capability from it's components that implements this interface.
 */
public interface ICapabilityComponent extends IMachineComponent {

    /**
     * @see net.minecraftforge.common.capabilities.ICapabilityProvider#getCapability(Capability, Direction)
     */
    <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side);

    /**
     * Called when the tile is removed, and the caability instances need to be invalidated.
     */
    void invalidateCapability();
}
