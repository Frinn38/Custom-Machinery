package fr.frinn.custommachinery.impl.component.config;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.EnumCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

public enum SideMode {

    INPUT(true, false, Component.translatable("custommachinery.side.input").withStyle(ChatFormatting.RED), FastColor.ARGB32.color(255, 255, 85, 85)),
    OUTPUT(false, true, Component.translatable("custommachinery.side.output").withStyle(ChatFormatting.BLUE), FastColor.ARGB32.color(255, 85, 85, 255)),
    BOTH(true, true, Component.translatable("custommachinery.side.both").withStyle(ChatFormatting.LIGHT_PURPLE), FastColor.ARGB32.color(255, 255, 85, 255)),
    NONE(false, false, Component.translatable("custommachinery.side.none").withStyle(ChatFormatting.GRAY), FastColor.ARGB32.color(0, 255, 255, 255));

    public static final NamedCodec<SideMode> CODEC = EnumCodec.of(SideMode.class);

    private final boolean isInput;
    private final boolean isOutput;
    private final Component title;
    private final int color;

    SideMode(boolean isInput, boolean isOutput, Component title, int color) {
        this.isInput = isInput;
        this.isOutput = isOutput;
        this.title = title;
        this.color = color;
    }

    public boolean isInput() {
        return this.isInput;
    }

    public boolean isOutput() {
        return this.isOutput;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public SideMode next() {
        return switch (this) {
            case INPUT -> OUTPUT;
            case OUTPUT -> BOTH;
            case BOTH -> NONE;
            case NONE -> INPUT;
        };
    }

    public SideMode previous() {
        return switch (this) {
            case INPUT -> NONE;
            case OUTPUT -> INPUT;
            case BOTH -> OUTPUT;
            case NONE -> BOTH;
        };
    }

    public Component title() {
        return this.title;
    }

    public int color() {
        return this.color;
    }
}
