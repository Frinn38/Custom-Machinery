package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.EmptyGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class EmptyGuiElementWidget extends AbstractGuiElementWidget<EmptyGuiElement> {
    public EmptyGuiElementWidget(EmptyGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.empty());
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

    }
}
