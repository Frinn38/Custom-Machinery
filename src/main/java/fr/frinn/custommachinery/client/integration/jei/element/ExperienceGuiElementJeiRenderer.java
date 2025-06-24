package fr.frinn.custommachinery.client.integration.jei.element;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import net.minecraft.client.gui.GuiGraphics;

public class ExperienceGuiElementJeiRenderer implements IJEIElementRenderer<ExperienceGuiElement> {

    @Override
    public void renderElementInJEI(GuiGraphics graphics, ExperienceGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        if(!element.getMode().isDisplayBar())
            ClientHandler.blit(graphics, element.getTexture(), posX, posY, width, height);
    }
}
