package fr.frinn.custommachinery.impl.client;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class TexturedGuiElementRenderable<T extends AbstractTexturedGuiElement> extends AbstractGuiElementRenderable<T> {

    public TexturedGuiElementRenderable(T element, IMachineScreen screen) {
        super(element, screen);
    }

    @Override
    public void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if(this.getElement().getTextureHovered() != null && this.isHovered(mouseX, mouseY))
            ClientHandler.blit(graphics, this.getElement().getTextureHovered(), this.getElement().getX(), this.getElement().getY(), this.getElement().getWidth(), this.getElement().getHeight());
        else
            ClientHandler.blit(graphics, this.getElement().getTexture(), this.getElement().getX(), this.getElement().getY(), this.getElement().getWidth(), this.getElement().getHeight());
    }
}
