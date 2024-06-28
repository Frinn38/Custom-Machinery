package fr.frinn.custommachinery.client.screen.widget;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.ComponentStylePopup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.function.Consumer;

public class ComponentEditBox extends GroupWidget {

    public static final WidgetSprites BUTTON_TEXTURE = new WidgetSprites(CustomMachinery.rl("creation/style/style_button"), CustomMachinery.rl("creation/style/style_button_hovered"));

    private final EditBox editBox;
    private final ImageButton button;
    private Style style = Style.EMPTY;

    public ComponentEditBox(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.editBox = this.addWidget(new EditBox(Minecraft.getInstance().font, x, y, width - 20, height, message));
        this.editBox.setFormatter((value, pos) -> FormattedCharSequence.forward(value, this.style));
        this.button = this.addWidget(new ImageButton(x + width - 20, y, 20, 20, BUTTON_TEXTURE, button -> this.button()));
    }

    private void button() {
        if(Minecraft.getInstance().screen instanceof BaseScreen baseScreen)
            baseScreen.openPopup(new ComponentStylePopup(baseScreen, this), "Edit machine name");
    }

    public Component getComponent() {
        return Component.translatable(this.editBox.getValue()).setStyle(this.style);
    }

    public String getValue() {
        return this.editBox.getValue();
    }

    public void setComponent(Component component) {
        this.setStyle(component.getStyle());
        this.editBox.setValue(component.getString());
    }

    public void setComponentResponder(Consumer<MutableComponent> responder) {
        this.editBox.setResponder(s -> responder.accept(Component.translatable(s).setStyle(this.style)));
    }

    public void setHint(Component hint) {
        this.editBox.setHint(hint);
    }

    public Style getStyle() {
        return this.style;
    }

    public void setStyle(Style style) {
        this.style = style;
        this.editBox.setValue(this.editBox.getValue());
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.editBox.keyPressed(keyCode, scanCode, modifiers);
    }
}
