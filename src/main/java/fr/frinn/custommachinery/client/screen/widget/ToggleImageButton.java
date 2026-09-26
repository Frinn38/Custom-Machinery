package fr.frinn.custommachinery.client.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;

public class ToggleImageButton extends ImageButton {

    private boolean toggle = false;

    public ToggleImageButton(int x, int y, int width, int height, WidgetSprites sprites, OnPress onPress) {
        super(x, y, width, height, sprites, onPress);
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        super.onClick(event, doubleClick);
        this.toggle = !this.toggle;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprites.get(this.toggle, this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height);
    }
}
