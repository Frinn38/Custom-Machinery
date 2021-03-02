package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;

public interface IGuiElementRenderer<E extends IGuiElement> {

    void renderElement(MatrixStack matrix, E element, CustomMachineScreen screen);

    void renderTooltip(MatrixStack matrix, E element, CustomMachineScreen screen, int mouseX, int mouseY);

    boolean isHovered(E element, CustomMachineScreen screen, int mouseX, int mouseY);
}
