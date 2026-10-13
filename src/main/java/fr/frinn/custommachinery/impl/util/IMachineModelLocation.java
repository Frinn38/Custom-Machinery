package fr.frinn.custommachinery.impl.util;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Define a way for the custom machine to know which model it should use for the block and item appearance.
 * Instances implementing this interface will be loaded server-side when the machine JSON is parsed, so client only classes are not acceptable here.
 */
public interface IMachineModelLocation {

    /**
     * Used for the machine block rendering.
     * Return either a {@link BlockState} to mimic the rendering, or an id pointing to a standalone model key.
     */
    interface Block extends IMachineModelLocation {
        Either<BlockState, Identifier> location();
    }

    /**
     * Used for the machine item rendering.
     * Return either a {@link ItemStack} to mimic the rendering, or an id pointing to a standalone model key.
     */
    interface Item extends IMachineModelLocation {
        Either<ItemStack, Identifier> location();
    }
}
