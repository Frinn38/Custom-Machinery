package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.guielement.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.api.recipe.IMachineRecipe;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.data.gui.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class ProgressGuiElementRenderer implements IGuiElementRenderer<ProgressBarGuiElement>, IJEIElementRenderer<ProgressBarGuiElement> {

    @Override
    public void renderElement(MatrixStack matrix, ProgressBarGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        int filledWidth = (int)(width * ((CustomMachineContainer)screen.getScreen().getContainer()).getRecipeProgressPercent());
        int filledHeight = (int)(height * ((CustomMachineContainer)screen.getScreen().getContainer()).getRecipeProgressPercent());

        Minecraft.getInstance().getTextureManager().bindTexture(element.getEmptyTexture());

        if(element.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && element.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE)) {
            matrix.push();
            rotate(matrix, element.getDirection(), posX, posY, width, height);

            AbstractGui.blit(matrix, 0, 0, 0, 0, width, height, width, height);
            Minecraft.getInstance().getTextureManager().bindTexture(element.getFilledTexture());
            AbstractGui.blit(matrix, 0, 0, 0, 0, filledWidth, height, width, height);

            matrix.pop();
        } else {
            AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
            Minecraft.getInstance().getTextureManager().bindTexture(element.getFilledTexture());
            switch (element.getDirection()) {
                case RIGHT:
                    AbstractGui.blit(matrix, posX, posY, 0, 0, filledWidth, height, width, height);
                    break;
                case LEFT:
                    AbstractGui.blit(matrix, posX + width - filledWidth, posY, width - filledWidth, 0, filledWidth, height, width, height);
                    break;
                case TOP:
                    AbstractGui.blit(matrix, posX, posY, 0, 0, width, filledHeight, width, height);
                    break;
                case BOTTOM:
                    AbstractGui.blit(matrix, posX, posY + height - filledHeight, 0, height - filledHeight, width, filledHeight, width, height);
                    break;
            }
        }
    }

    @Override
    public void renderTooltip(MatrixStack matrix, ProgressBarGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {

    }

    @Override
    public boolean isHovered(ProgressBarGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        boolean invertAxis = element.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && element.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE) && element.getDirection() != ProgressBarGuiElement.Direction.RIGHT && element.getDirection() != ProgressBarGuiElement.Direction.LEFT;
        int width = invertAxis ? element.getHeight() : element.getWidth();
        int height = invertAxis ? element.getWidth() : element.getHeight();
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

        int filledWidth = (int)(Minecraft.getInstance().world.getGameTime() % width);
        int filledHeight = (int)(Minecraft.getInstance().world.getGameTime() % height);

        Minecraft.getInstance().getTextureManager().bindTexture(element.getEmptyTexture());

        if(element.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && element.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE)) {
            matrix.push();
            rotate(matrix, element.getDirection(), posX, posY, width, height);

            AbstractGui.blit(matrix, 0, 0, 0, 0, width, height, width, height);
            Minecraft.getInstance().getTextureManager().bindTexture(element.getFilledTexture());
            AbstractGui.blit(matrix, 0, 0, 0, 0, filledWidth, height, width, height);

            matrix.pop();
        } else {
            AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
            Minecraft.getInstance().getTextureManager().bindTexture(element.getFilledTexture());
            switch (element.getDirection()) {
                case RIGHT:
                    AbstractGui.blit(matrix, posX, posY, 0, 0, filledWidth, height, width, height);
                    break;
                case LEFT:
                    AbstractGui.blit(matrix, posX + width - filledWidth, posY, width - filledWidth, 0, filledWidth, height, width, height);
                    break;
                case TOP:
                    AbstractGui.blit(matrix, posX, posY, 0, 0, width, filledHeight, width, height);
                    break;
                case BOTTOM:
                    AbstractGui.blit(matrix, posX, posY + height - filledHeight, 0, height - filledHeight, width, filledHeight, width, height);
                    break;
            }
        }
    }

    @Override
    public boolean isHoveredInJei(ProgressBarGuiElement element, int posX, int posY, int mouseX, int mouseY) {
        boolean invertAxis = element.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && element.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE) && element.getDirection() != ProgressBarGuiElement.Direction.RIGHT && element.getDirection() != ProgressBarGuiElement.Direction.LEFT;
        int width = invertAxis ? element.getHeight() : element.getWidth();
        int height = invertAxis ? element.getWidth() : element.getHeight();
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }

    @Override
    public List<ITextComponent> getJEITooltips(ProgressBarGuiElement element, IMachineRecipe recipe) {
        List<ITextComponent> tooltips = new ArrayList<>();
        tooltips.add(new TranslationTextComponent("custommachinery.jei.recipe.time", recipe.getRecipeTime()));
        if(!CMConfig.INSTANCE.needAdvancedInfoForRecipeID.get() || Minecraft.getInstance().gameSettings.advancedItemTooltips)
            tooltips.add(new StringTextComponent(recipe.getId().toString()).mergeStyle(TextFormatting.DARK_GRAY));
        return tooltips;
    }

    private void rotate(MatrixStack matrix, ProgressBarGuiElement.Direction direction, int posX, int posY, int width, int height) {
        switch (direction) {
            case RIGHT:
                matrix.translate(posX, posY, 0);
                break;
            case LEFT:
                matrix.rotate(Vector3f.ZP.rotationDegrees(180));
                matrix.translate(-width - posX, -height - posY, 0);
                break;
            case TOP:
                matrix.rotate(Vector3f.ZP.rotationDegrees(270));
                matrix.translate(-width - posY, posX, 0);
                break;
            case BOTTOM:
                matrix.rotate(Vector3f.ZP.rotationDegrees(90));
                matrix.translate(posY, -height - posX, 0);
                break;
        }
    }
}
