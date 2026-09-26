package fr.frinn.custommachinery.impl.component.config;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.component.config.SideConfig.SideMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public enum ToggleSideMode implements SideMode {

    ENABLED(Component.translatable("custommachinery.side.enabled").withStyle(ChatFormatting.LIGHT_PURPLE), ARGB.color(255, 255, 85, 255)),
    DISABLED(Component.translatable("custommachinery.side.disabled").withStyle(ChatFormatting.GRAY), ARGB.color(0, 255, 255, 255));

    public static final NamedCodec<ToggleSideMode> CODEC = NamedCodec.enumCodec(ToggleSideMode.class);

    private final Component title;
    private final int color;

    ToggleSideMode(Component title, int color) {
        this.title = title;
        this.color = color;
    }

    public boolean isEnabled() {
        return this == ENABLED;
    }

    public boolean isDisabled() {
        return this == DISABLED;
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
