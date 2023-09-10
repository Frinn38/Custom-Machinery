package fr.frinn.custommachinery.client.integration.jei.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.TextureGuiElement;
import net.minecraft.client.gui.GuiComponent;

public class TextureGuiElementJeiRenderer implements IJEIElementRenderer<TextureGuiElement> {


    @Override
    public void renderElementInJEI(PoseStack matrix, TextureGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        if(element.getTextureHovered() != null && isHoveredInJei(element, element.getX(), element.getY(), mouseX, mouseY))
            ClientHandler.bindTexture(element.getTextureHovered());
        else
            ClientHandler.bindTexture(element.getTexture());

        GuiComponent.blit(matrix, element.getX(), element.getY(), 0, 0, element.getWidth(), element.getHeight(), element.getWidth(), element.getHeight());
    }
}
