package fr.frinn.custommachinery.impl.guielement;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

public class TexturedGuiElementWidget<T extends AbstractTexturedGuiElement> extends AbstractGuiElementWidget<T> {


    public TexturedGuiElementWidget(T element, IMachineScreen screen, Component title) {
        super(element, screen, title);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        if(this.getElement().getTextureHovered() != null && this.isHoveredOrFocused())
            RenderSystem.setShaderTexture(0, this.getElement().getTextureHovered());
        else
            RenderSystem.setShaderTexture(0, this.getElement().getTexture());
        GuiComponent.blit(poseStack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
    }
}
