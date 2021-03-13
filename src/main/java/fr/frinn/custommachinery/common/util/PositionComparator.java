package fr.frinn.custommachinery.common.util;

import com.mojang.serialization.Codec;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public class PositionComparator {

    public static final Codec<PositionComparator> CODEC = Codec.STRING.xmap(PositionComparator::new, PositionComparator::toString).stable();

    private Direction.Axis axis;
    private MODE mode;
    private int coordinate;

    public PositionComparator(String s) {
        this.axis = Direction.Axis.byName(s.substring(0, 1));
        this.mode = MODE.fromPrefix(s.substring(1, 3));
        this.coordinate = Integer.parseInt(s.substring(3));
    }

    public String toString() {
        return this.axis.toString() + this.mode.prefix + this.coordinate;
    }

    public boolean compare(BlockPos pos) {
        int toCompare = 0;
        switch (this.axis) {
            case X:
                toCompare = pos.getX();
                break;
            case Y:
                toCompare = pos.getY();
                break;
            case Z:
                toCompare = pos.getZ();
                break;
        }
        switch (this.mode) {
            case UPPER:
                return toCompare > this.coordinate;
            case UPPER_OR_EQUALS:
                return toCompare >= this.coordinate;
            case EQUALS:
                return toCompare == this.coordinate;
            case LESSER_OR_EQUALS:
                return toCompare <= this.coordinate;
            case LESSER:
                return toCompare < this.coordinate;
            default:
                return false;
        }
    }

    private enum MODE {
        UPPER(">>"),
        UPPER_OR_EQUALS(">="),
        EQUALS("=="),
        LESSER_OR_EQUALS("<="),
        LESSER("<<");

        private String prefix;

        MODE(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return this.prefix;
        }

        public static PositionComparator.MODE fromPrefix(String prefix) {
            return Arrays.stream(values()).filter(mode -> mode.prefix.equals(prefix)).findFirst().orElseThrow(() -> new RuntimeException("Invalid Position Comparator prefix: " + prefix));
        }
    }
}
