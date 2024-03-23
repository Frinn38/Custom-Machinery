package fr.frinn.custommachinery.client.screen.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class ComponentEditBox extends EditBox {

    private Style style = Style.EMPTY;

    public ComponentEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        this.setFormatter((value, pos) -> FormattedCharSequence.forward(value, this.style));
    }

    public Component getComponent() {
        return Component.translatable(this.getValue()).setStyle(this.style);
    }

    public void setComponent(Component component) {
        this.setValue(component.getString());
        this.setStyle(component.getStyle());
    }

    public Style getStyle() {
        return this.style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public void invert(ChatFormatting format) {
        this.style = switch (format) {
            case BOLD -> this.style.withBold(!this.style.isBold());
            case ITALIC -> this.style.withItalic(!this.style.isItalic());
            case UNDERLINE -> this.style.withUnderlined(!this.style.isUnderlined());
            case STRIKETHROUGH -> this.style.withStrikethrough(!this.style.isStrikethrough());
            case OBFUSCATED -> this.style.withObfuscated(!this.style.isObfuscated());
            default -> this.style;
        };
    }
}
