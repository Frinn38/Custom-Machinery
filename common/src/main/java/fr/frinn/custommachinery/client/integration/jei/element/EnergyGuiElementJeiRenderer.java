package fr.frinn.custommachinery.client.integration.jei.element;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import net.minecraft.client.gui.GuiGraphics;

public class EnergyGuiElementJeiRenderer implements IJEIElementRenderer<EnergyGuiElement> {

    @Override
    public void renderElementInJEI(GuiGraphics graphics, EnergyGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        graphics.blit(element.getEmptyTexture(), posX, posY, 0, 0, width, height, width, height);
    }
}
