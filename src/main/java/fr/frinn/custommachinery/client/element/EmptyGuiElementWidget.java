package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.EmptyGuiElement;
import fr.frinn.custommachinery.impl.client.AbstractGuiElementRenderable;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class EmptyGuiElementWidget extends AbstractGuiElementRenderable<EmptyGuiElement> {
    public EmptyGuiElementWidget(EmptyGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }

    @Override
    public void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {

    }
}
