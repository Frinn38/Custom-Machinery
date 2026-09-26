package fr.frinn.custommachinery.impl.component.config;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.EnumCodec;
import fr.frinn.custommachinery.impl.component.config.SideConfig.SideMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public enum IOSideMode implements SideMode {

    INPUT(true, false, Component.translatable("custommachinery.side.input").withStyle(ChatFormatting.RED), ARGB.color(255, 255, 85, 85)),
    OUTPUT(false, true, Component.translatable("custommachinery.side.output").withStyle(ChatFormatting.BLUE), ARGB.color(255, 85, 85, 255)),
    BOTH(true, true, Component.translatable("custommachinery.side.both").withStyle(ChatFormatting.LIGHT_PURPLE), ARGB.color(255, 255, 85, 255)),
    NONE(false, false, Component.translatable("custommachinery.side.none").withStyle(ChatFormatting.GRAY), ARGB.color(0, 255, 255, 255));

    public static final NamedCodec<IOSideMode> CODEC = EnumCodec.of(IOSideMode.class);

    private final boolean isInput;
    private final boolean isOutput;
    private final Component title;
    private final int color;

    IOSideMode(boolean isInput, boolean isOutput, Component title, int color) {
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

    public IOSideMode next() {
        return switch (this) {
            case INPUT -> OUTPUT;
            case OUTPUT -> BOTH;
            case BOTH -> NONE;
            case NONE -> INPUT;
        };
    }

    public IOSideMode previous() {
        return switch (this) {
            case INPUT -> NONE;
            case OUTPUT -> INPUT;
            case BOTH -> OUTPUT;
            case NONE -> BOTH;
        };
    }

    @Override
    public Component title() {
        return this.title;
    }

    @Override
    public int color() {
        return this.color;
    }
}
