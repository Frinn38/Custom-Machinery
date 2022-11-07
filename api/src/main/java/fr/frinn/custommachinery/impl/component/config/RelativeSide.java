package fr.frinn.custommachinery.impl.component.config;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.impl.codec.EnumCodec;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Mostly copied from Mekanism API:
 * <a href="https://github.com/mekanism/Mekanism/blob/1.18.x/src/api/java/mekanism/api/RelativeSide.java">See on Github</a>
 */

public enum RelativeSide {
    TOP,
    BOTTOM,
    FRONT,
    RIGHT,
    BACK,
    LEFT;

    public static final Codec<RelativeSide> CODEC = EnumCodec.of(RelativeSide.class);

    public Component getTranslationName() {
        return new TranslatableComponent("custommachinery.side." + name().toLowerCase(Locale.ROOT));
    }

    /**
     * Gets the {@link Direction} from the block based on what side it is facing.
     *
     * @param facing The direction the block is facing.
     *
     * @return The direction representing which side of the block this RelativeSide is actually representing based on the direction it is facing.
     */
    public Direction getDirection(@NotNull Direction facing) {
        return switch (this) {
            case FRONT -> facing;
            case BACK -> facing.getOpposite();
            case LEFT -> facing == Direction.DOWN || facing == Direction.UP ? Direction.EAST : facing.getClockWise();
            case RIGHT -> facing == Direction.DOWN || facing == Direction.UP ? Direction.WEST : facing.getCounterClockWise();
            case TOP -> switch (facing) {
                case DOWN -> Direction.NORTH;
                case UP -> Direction.SOUTH;
                default -> Direction.UP;
            };
            case BOTTOM -> switch (facing) {
                case DOWN -> Direction.SOUTH;
                case UP -> Direction.NORTH;
                default -> Direction.DOWN;
            };
        };
    }

    /**
     * Gets the {@link RelativeSide} based on a side, and the facing direction of a block.
     *
     * @param facing The direction the block is facing.
     * @param side   The side of the block we want to know what {@link RelativeSide} it is.
     *
     * @return the {@link RelativeSide} based on a side, and the facing direction of a block.
     *
     * @apiNote The calculations for what side is what when facing upwards or downwards, is done as if it was facing NORTH and rotated around the X-axis
     */
    public static RelativeSide fromDirections(@NotNull Direction facing, @NotNull Direction side) {
        if (side == facing) {
            return FRONT;
        } else if (side == facing.getOpposite()) {
            return BACK;
        } else if (facing == Direction.DOWN || facing == Direction.UP) {
            return switch (side) {
                case NORTH -> facing == Direction.DOWN ? TOP : BOTTOM;
                case SOUTH -> facing == Direction.DOWN ? BOTTOM : TOP;
                case WEST -> RIGHT;
                case EAST -> LEFT;
                default -> throw new IllegalStateException("Case should have been caught earlier.");
            };
        } else if (side == Direction.DOWN) {
            return BOTTOM;
        } else if (side == Direction.UP) {
            return TOP;
        } else if (side == facing.getCounterClockWise()) {
            return RIGHT;
        } else if (side == facing.getClockWise()) {
            return LEFT;
        }
        //Fall back to front, should never get here
        return FRONT;
    }
}
