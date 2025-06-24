package fr.frinn.custommachinery.impl.guielement;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TexturedGuiElementWidget<T extends AbstractTexturedGuiElement> extends AbstractGuiElementWidget<T> {

    public TexturedGuiElementWidget(T element, IMachineScreen screen, Component title) {
        super(element, screen, title);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if(this.getElement().getTextureHovered() != null && this.isHovered())
            ClientHandler.blit(graphics, this.getElement().getTextureHovered(), this.getX(), this.getY(), this.width, this.height);
        else
            ClientHandler.blit(graphics, this.getElement().getTexture(), this.getX(), this.getY(), this.width, this.height);
    }
}
