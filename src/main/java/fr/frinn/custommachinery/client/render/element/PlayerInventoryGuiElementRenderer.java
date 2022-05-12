package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.data.gui.PlayerInventoryGuiElement;
import net.minecraft.client.gui.GuiComponent;

public class PlayerInventoryGuiElementRenderer implements IGuiElementRenderer<PlayerInventoryGuiElement> {

    @Override
    public void renderElement(PoseStack matrix, PlayerInventoryGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        ClientHandler.bindTexture(element.getTexture());
        GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }

    @Override
    public void renderTooltip(PoseStack matrix, PlayerInventoryGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {

    }

    @Override
    public boolean isHovered(PlayerInventoryGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        return false;
    }
}
