package fr.frinn.custommachinery.client.integration.jei.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.common.guielement.TextGuiElement;
import net.minecraft.client.Minecraft;

public class TextGuiElementJeiRenderer implements IJEIElementRenderer<TextGuiElement> {

    @Override
    public void renderElementInJEI(PoseStack matrix, TextGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        int posX = switch (element.getAlignment()) {
            case CENTER -> element.getX() - Minecraft.getInstance().font.width(element.getText().getString()) / 2;
            case RIGHT -> element.getX() - Minecraft.getInstance().font.width(element.getText().getString());
            default -> element.getX();
        };
        int posY = element.getY();
        Minecraft.getInstance().font.draw(matrix, element.getText(), posX, posY, element.getColor());
    }
}
