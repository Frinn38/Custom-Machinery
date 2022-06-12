package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.client.ClientHandler;
import net.minecraft.client.gui.GuiComponent;

public abstract class TexturedGuiElementRenderer<T extends AbstractTexturedGuiElement> implements IGuiElementRenderer<T> {

    @Override
    public void renderElement(PoseStack matrix, T element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        ClientHandler.bindTexture(element.getTexture());
        GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }
}
