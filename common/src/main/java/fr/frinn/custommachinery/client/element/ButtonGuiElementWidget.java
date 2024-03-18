package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.ButtonGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.CButtonGuiElementPacket;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ButtonGuiElementWidget extends AbstractGuiElementWidget<ButtonGuiElement> {

    private static final Component TITLE = Component.literal("Button");

    public ButtonGuiElementWidget(ButtonGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture;
        if(getElement().isToogle() && getScreen().getTile().getComponentManager().getComponent(Registration.DATA_MACHINE_COMPONENT.get()).map(component -> component.getData().getBoolean(getElement().getId())).orElse(false)) {
            if(this.isHovered())
                texture = this.getElement().getBaseTextureToogleHovered();
            else
                texture = this.getElement().getTextureToogle();
        } else {
            if(this.isHovered())
                texture = this.getElement().getTextureHovered();
            else
                texture = this.getElement().getTexture();
        }

        graphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);

        if(!getElement().getText().getString().isEmpty())
            graphics.drawString(Minecraft.getInstance().font, this.getElement().getText(), (int)(this.getX() + this.width / 2.0f - Minecraft.getInstance().font.width(getElement().getText()) / 2.0f), (int)(this.getY() + this.height / 2.0f - Minecraft.getInstance().font.lineHeight / 2.0f), 0);

        if(!getElement().getItem().isEmpty())
            graphics.renderItem(getElement().getItem(), (int)(this.getX() + this.width / 2.0f - 8), (int)(this.getY() + this.height / 2.0f - 8));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        new CButtonGuiElementPacket(getElement().getId(), getElement().isToogle()).sendToServer();
    }
}
