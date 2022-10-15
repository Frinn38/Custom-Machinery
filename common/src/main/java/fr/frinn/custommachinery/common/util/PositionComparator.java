package fr.frinn.custommachinery.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class PositionComparator {

    private Direction.Axis axis;
    private ComparatorMode mode;
    private int coordinate;

    public PositionComparator(String s) {
        this.axis = Direction.Axis.byName(s.substring(0, 1));
        this.mode = ComparatorMode.value(s.substring(1, 3));
        this.coordinate = Integer.parseInt(s.substring(3));
    }

    public String toString() {
        return this.axis.toString() + this.mode.getPrefix() + this.coordinate;
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
        return this.mode.compare(toCompare, this.coordinate);
    }

    public Component getText() {
        return new TextComponent(this.axis.getSerializedName() + " " + new TranslatableComponent(this.mode.getTranslationKey()).getString() + " " + this.coordinate);
    }
}
