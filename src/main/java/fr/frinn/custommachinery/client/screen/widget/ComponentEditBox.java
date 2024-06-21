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

public class ComponentEditBox extends EditBox {

    public static final WidgetSprites BUTTON_TEXTURE = new WidgetSprites(CustomMachinery.rl("textures/gui/creation/style/style_button.png"), CustomMachinery.rl("textures/gui/creation/style/style_button_hovered.png"));

    private final ImageButton button;
    private Style style = Style.EMPTY;

    public ComponentEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width - 20, height, message);
        this.button = new ImageButton(x + width + 2, y, 20, 20, BUTTON_TEXTURE, button -> this.button());
        this.setFormatter((value, pos) -> FormattedCharSequence.forward(value, this.style));
    }

    private void button() {
        if(Minecraft.getInstance().screen instanceof BaseScreen baseScreen)
            baseScreen.openPopup(new ComponentStylePopup(baseScreen, this), "Edit machine name");
    }

    public Component getComponent() {
        return Component.translatable(this.getValue()).setStyle(this.style);
    }

    public void setComponent(Component component) {
        this.setStyle(component.getStyle());
        this.setValue(component.getString());
    }

    public void setComponentResponder(Consumer<MutableComponent> responder) {
        super.setResponder(s -> responder.accept(Component.translatable(s).setStyle(this.style)));
    }

    public Style getStyle() {
        return this.style;
    }

    public void setStyle(Style style) {
        this.style = style;
        this.setValue(this.getValue());
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
    public void setX(int x) {
        super.setX(x);
        this.button.setX(x + this.width + 2);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.button.setY(y);
    }

    @Override
    public int getWidth() {
        return super.getWidth() + 20;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.button.render(graphics, mouseX, mouseY, partialTick);
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.button.mouseClicked(mouseX, mouseY, button))
            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
