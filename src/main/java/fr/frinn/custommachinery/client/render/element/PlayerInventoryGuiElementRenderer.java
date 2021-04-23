package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.data.gui.PlayerInventoryGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class PlayerInventoryGuiElementRenderer implements IGuiElementRenderer<PlayerInventoryGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, PlayerInventoryGuiElement element, CustomMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        Minecraft.getInstance().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }

    @Override
    public void renderTooltip(MatrixStack matrix, PlayerInventoryGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {

    }

    @Override
    public boolean isHovered(PlayerInventoryGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {
        return false;
    }
}
