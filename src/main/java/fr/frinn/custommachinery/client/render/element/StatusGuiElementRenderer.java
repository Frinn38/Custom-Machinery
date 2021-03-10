package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.client.TextureSizeHelper;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.data.gui.StatusGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class StatusGuiElementRenderer implements IGuiElementRenderer<StatusGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, StatusGuiElement element, CustomMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        switch (screen.getTile().craftingManager.getStatus()) {
            case IDLE:
                Minecraft.getInstance().getTextureManager().bindTexture(element.getIdleTexture());
                break;
            case RUNNING:
                Minecraft.getInstance().getTextureManager().bindTexture(element.getRunningTexture());
                break;
            case ERRORED:
                Minecraft.getInstance().getTextureManager().bindTexture(element.getErroredTexture());
                break;
        }
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }

    @Override
    public void renderTooltip(MatrixStack matrix, StatusGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {

    }

    @Override
    public boolean isHovered(StatusGuiElement element, CustomMachineScreen screen, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }
}
