package fr.frinn.custommachinery.client.integration.jei.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.common.guielement.TextGuiElement;
import net.minecraft.client.Minecraft;

public class TextGuiElementJeiRenderer implements IJEIElementRenderer<TextGuiElement> {

    @Override
    public void renderElementInJEI(PoseStack pose, TextGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        int posX = switch (element.getAlignment()) {
            case CENTER -> element.getX() - Minecraft.getInstance().font.width(element.getText().getString()) / 2;
            case RIGHT -> element.getX() - Minecraft.getInstance().font.width(element.getText().getString());
            default -> element.getX();
        };
        int posY = element.getY();

        pose.pushPose();
        float scaleX = 1.0F;
        float scaleY = 1.0F;
        if(element.getWidth() >= 0)
            scaleX = (float)element.getWidth() / (float)Minecraft.getInstance().font.width(element.getText());

        if(element.getHeight() >= 0)
            scaleY = (float)element.getHeight() / (float)Minecraft.getInstance().font.lineHeight;

        if(scaleX == 1.0F && scaleY != 1.0F)
            scaleX = scaleY;
        else if(scaleX != 1.0F && scaleY == 1.0F)
            scaleY = scaleX;

        if(scaleX != 1.0F) {
            pose.translate(element.getX(), element.getY(), 0);
            pose.scale(scaleX, scaleY, 1.0F);
            pose.translate(-element.getX(), -element.getY(), 0);
        }
        Minecraft.getInstance().font.draw(pose, element.getText(), posX, posY, 0);
        pose.popPose();
    }
}
