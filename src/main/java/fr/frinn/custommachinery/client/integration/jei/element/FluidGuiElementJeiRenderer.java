package fr.frinn.custommachinery.client.integration.jei.element;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import net.minecraft.client.gui.GuiGraphics;

public class FluidGuiElementJeiRenderer implements IJEIElementRenderer<FluidGuiElement> {

    @Override
    public void renderElementInJEI(GuiGraphics graphics, FluidGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        graphics.blit(element.getTexture(), posX, posY, 0, 0, width, height, width, height);
    }
}
