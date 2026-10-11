package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.TextureGuiElement;
import fr.frinn.custommachinery.impl.client.TexturedGuiElementRenderable;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class TextureGuiElementWidget extends TexturedGuiElementRenderable<TextureGuiElement> {

    public TextureGuiElementWidget(TextureGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }

    @Override
    public void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushMatrix();
        if(this.getElement().getZLevel() != 0)
            graphics.nextStratum();
        super.extractRenderableRenderState(graphics, mouseX, mouseY, partialTicks);
        graphics.pose().popMatrix();
    }
}
