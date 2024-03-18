package fr.frinn.custommachinery.client.integration.jei.element;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.common.guielement.TextureGuiElement;
import net.minecraft.client.gui.GuiGraphics;

public class TextureGuiElementJeiRenderer implements IJEIElementRenderer<TextureGuiElement> {


    @Override
    public void renderElementInJEI(GuiGraphics graphics, TextureGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        if(element.getTextureHovered() != null && isHoveredInJei(element, element.getX(), element.getY(), mouseX, mouseY))
            graphics.blit(element.getTextureHovered(), element.getX(), element.getY(), 0, 0, element.getWidth(), element.getHeight(), element.getWidth(), element.getHeight());
        else
            graphics.blit(element.getTexture(), element.getX(), element.getY(), 0, 0, element.getWidth(), element.getHeight(), element.getWidth(), element.getHeight());
    }
}
