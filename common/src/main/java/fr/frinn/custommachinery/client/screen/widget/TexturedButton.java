package fr.frinn.custommachinery.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.client.ClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TexturedButton extends Button {

    private final ResourceLocation texture;
    private final boolean background;

    public TexturedButton(int x, int y, int width, int height, Component title, ResourceLocation texture, OnPress pressedAction, OnTooltip onTooltip) {
        super(x, y, width, height, title, pressedAction, onTooltip);
        this.texture = texture;
        this.background = false;
    }

    public TexturedButton(int x, int y, int width, int height, Component title, ResourceLocation texture, OnPress pressedAction, OnTooltip onTooltip, boolean background) {
        super(x, y, width, height, title, pressedAction, onTooltip);
        this.texture = texture;
        this.background = background;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        if(this.background)
            renderBg(pose, Minecraft.getInstance(), mouseX, mouseY);
        ClientHandler.bindTexture(this.texture);
        blit(pose, this.x, this.y, this.width, this.height, 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    protected void renderBg(PoseStack pose, Minecraft minecraft, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(pose, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(pose, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
    }
}
