package fr.frinn.custommachinery.impl.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Define a way for the custom machine to know which {@link net.minecraft.client.resources.model.BakedModel} it should use for the block and item appearance.
 * Instances implementing this interface will be loaded server-side when the machine json is parsed, so client only classes are not acceptable here like {@link net.minecraft.client.resources.model.ModelResourceLocation}.
 */
public interface IMachineModelLocation {

    @Nullable
    BlockState getState();

    @Nullable
    ResourceLocation getLoc();

    @Nullable
    String getProperties();

    String toString();
}
