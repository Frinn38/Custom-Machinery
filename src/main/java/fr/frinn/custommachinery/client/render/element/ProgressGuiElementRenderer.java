package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.guielement.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.api.recipe.IMachineRecipe;
import fr.frinn.custommachinery.common.data.gui.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;

public class ProgressGuiElementRenderer implements IGuiElementRenderer<ProgressBarGuiElement>, IJEIElementRenderer<ProgressBarGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, ProgressBarGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        Minecraft.getInstance().getTextureManager().bindTexture(element.getEmptyTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        int filledWidth = (int)(width * ((CustomMachineContainer)screen.getScreen().getContainer()).getRecipeProgressPercent());
        Minecraft.getInstance().getTextureManager().bindTexture(element.getFilledTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, filledWidth, height, width, height);
    }

    @Override
    public void renderTooltip(MatrixStack matrix, ProgressBarGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {

    }

    @Override
    public boolean isHovered(ProgressBarGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }

    @Override
    public void renderElementInJEI(MatrixStack matrix, ProgressBarGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        if(Minecraft.getInstance().world == null)
            return;

        Minecraft.getInstance().getTextureManager().bindTexture(element.getEmptyTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        int filledWidth = (int)Minecraft.getInstance().world.getGameTime() % width;
        Minecraft.getInstance().getTextureManager().bindTexture(element.getFilledTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, filledWidth, height, width, height);
    }

    @Override
    public List<ITextComponent> getJEITooltips(ProgressBarGuiElement element, IMachineRecipe recipe) {
        return Collections.singletonList(new TranslationTextComponent("custommachinery.jei.recipe.time", recipe.getRecipeTime()));
    }
}
