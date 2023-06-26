package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.ButtonGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.CButtonGuiElementPacket;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class ButtonGuiElementWidget extends AbstractGuiElementWidget<ButtonGuiElement> {

    private static final Component TITLE = new TextComponent("Button");

    public ButtonGuiElementWidget(ButtonGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        if(getElement().isToogle() && getScreen().getTile().getComponentManager().getComponent(Registration.DATA_MACHINE_COMPONENT.get()).map(component -> component.getData().getBoolean(getElement().getId())).orElse(false))
            RenderSystem.setShaderTexture(0, getElement().getTextureToogle());
        else
            RenderSystem.setShaderTexture(0, getElement().getTexture());
        GuiComponent.blit(poseStack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        if(isHoveredOrFocused())
            ClientHandler.renderButtonHover(poseStack, this.x, this.y, this.width, this.height);
        if(!getElement().getText().getString().isEmpty())
            Minecraft.getInstance().font.draw(poseStack, getElement().getText(), this.x + this.width / 2.0f - Minecraft.getInstance().font.width(getElement().getText()) / 2.0f, this.y + this.height / 2.0f - Minecraft.getInstance().font.lineHeight / 2.0f, 0);
        if(!getElement().getItem().isEmpty())
            Minecraft.getInstance().getItemRenderer().renderGuiItem(getElement().getItem(), (int)(this.x + this.width / 2.0f - 8), (int)(this.y + this.height / 2.0f - 8));
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        new CButtonGuiElementPacket(getElement().getId(), getElement().isToogle()).sendToServer();
    }
}
