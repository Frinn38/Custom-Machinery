package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.data.gui.TextGuiElement;
import net.minecraft.client.Minecraft;

public class TextGuiElementRenderer implements IGuiElementRenderer<TextGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, TextGuiElement element, IMachineScreen screen) {
        int posX;
        switch (element.getAlignment()) {
            case CENTER:
                posX = element.getX() - Minecraft.getInstance().fontRenderer.getStringWidth(element.getText().getString()) / 2;
                break;
            case RIGHT:
                posX = element.getX() - Minecraft.getInstance().fontRenderer.getStringWidth(element.getText().getString());
                break;
            default:
                posX = element.getX();
        }
        int posY = element.getY();
        Minecraft.getInstance().fontRenderer.drawText(matrix, element.getText(), posX, posY, element.getColor());
    }

    @Override
    public void renderTooltip(MatrixStack matrix, TextGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {

    }

    @Override
    public boolean isHovered(TextGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        return false;
    }
}
