package fr.frinn.custommachinery.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

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

    public void renderTexture(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int uOffset, int vOffset, int textureDifference, int width, int height, int textureWidth, int textureHeight) {
        int i = vOffset;
        if (!this.isActive())
            i += textureDifference * 2;
        else if (this.isHovered() || this.toggle)
            i += textureDifference;

        RenderSystem.enableDepthTest();
        guiGraphics.blit(texture, x, y, uOffset, i, width, height, textureWidth, textureHeight);
    }
}
