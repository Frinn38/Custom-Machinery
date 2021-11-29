package fr.frinn.custommachinery.apiimpl.guielement;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;

public class DummyGuiElementRenderer implements IGuiElementRenderer<IGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, IGuiElement element, IMachineScreen screen) {

    }

    @Override
    public void renderTooltip(MatrixStack matrix, IGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {

    }

    @Override
    public boolean isHovered(IGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        return false;
    }
}
