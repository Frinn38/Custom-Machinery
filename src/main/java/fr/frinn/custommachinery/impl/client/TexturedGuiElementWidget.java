package fr.frinn.custommachinery.impl.client;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.client.screen.ScreenUtils;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class TexturedGuiElementWidget<T extends AbstractTexturedGuiElement> extends AbstractGuiElementWidget<T> {

    public TexturedGuiElementWidget(T element, IMachineScreen screen, Component title) {
        super(element, screen, title);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if(this.getElement().getTextureHovered() != null && this.isHovered())
            ScreenUtils.blit(graphics, this.getElement().getTextureHovered(), this.getX(), this.getY(), this.width, this.height);
        else
            ScreenUtils.blit(graphics, this.getElement().getTexture(), this.getX(), this.getY(), this.width, this.height);
    }
}
