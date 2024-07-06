package fr.frinn.custommachinery.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;

public class ToggleImageButton extends ImageButton {

    private boolean toggle = false;

    public ToggleImageButton(int x, int y, int width, int height, WidgetSprites sprites, OnPress onPress) {
        super(x, y, width, height, sprites, onPress);
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.toggle = !this.toggle;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.blitSprite(this.sprites.get(this.toggle, this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height);
    }
}
