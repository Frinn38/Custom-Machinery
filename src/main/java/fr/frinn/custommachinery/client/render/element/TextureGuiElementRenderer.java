package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.data.gui.TextureGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class TextureGuiElementRenderer implements IGuiElementRenderer<TextureGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, TextureGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        Minecraft.getInstance().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }

    @Override
    public void renderTooltip(MatrixStack matrix, TextureGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {

    }

    @Override
    public boolean isHovered(TextureGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        return false;
    }
}
