package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class ProgressGuiElementRenderer implements IGuiElementRenderer<ProgressBarGuiElement>, IJEIElementRenderer<ProgressBarGuiElement> {

    @Override
    public void renderElement(PoseStack matrix, ProgressBarGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        int filledWidth = (int)(width * Mth.clamp(((CustomMachineContainer)screen.getScreen().getMenu()).getRecipeProgressPercent(), 0.0D, 1.0D));
        int filledHeight = (int)(height * Mth.clamp(((CustomMachineContainer)screen.getScreen().getMenu()).getRecipeProgressPercent(), 0.0D, 1.0D));

        ClientHandler.bindTexture(element.getEmptyTexture());

        if(element.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && element.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE)) {
            matrix.pushPose();
            rotate(matrix, element.getDirection(), posX, posY, width, height);

            GuiComponent.blit(matrix, 0, 0, 0, 0, width, height, width, height);
            ClientHandler.bindTexture(element.getFilledTexture());
            GuiComponent.blit(matrix, 0, 0, 0, 0, filledWidth, height, width, height);

            matrix.popPose();
        } else {
            GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
            ClientHandler.bindTexture(element.getFilledTexture());
            switch (element.getDirection()) {
                case RIGHT -> GuiComponent.blit(matrix, posX, posY, 0, 0, filledWidth, height, width, height);
                case LEFT -> GuiComponent.blit(matrix, posX + width - filledWidth, posY, width - filledWidth, 0, filledWidth, height, width, height);
                case TOP -> GuiComponent.blit(matrix, posX, posY, 0, 0, width, filledHeight, width, height);
                case BOTTOM -> GuiComponent.blit(matrix, posX, posY + height - filledHeight, 0, height - filledHeight, width, filledHeight, width, height);
            }
        }
    }

    @Override
    public void renderTooltip(PoseStack matrix, ProgressBarGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {

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
    public void renderElementInJEI(PoseStack matrix, ProgressBarGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        if(Minecraft.getInstance().level == null)
            return;

        int filledWidth = (int)(Minecraft.getInstance().level.getGameTime() % width);
        int filledHeight = (int)(Minecraft.getInstance().level.getGameTime() % height);

        ClientHandler.bindTexture(element.getEmptyTexture());

        if(element.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && element.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE)) {
            matrix.pushPose();
            rotate(matrix, element.getDirection(), posX, posY, width, height);

            GuiComponent.blit(matrix, 0, 0, 0, 0, width, height, width, height);
            ClientHandler.bindTexture(element.getFilledTexture());
            GuiComponent.blit(matrix, 0, 0, 0, 0, filledWidth, height, width, height);

            matrix.popPose();
        } else {
            GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
            ClientHandler.bindTexture(element.getFilledTexture());
            switch (element.getDirection()) {
                case RIGHT -> GuiComponent.blit(matrix, posX, posY, 0, 0, filledWidth, height, width, height);
                case LEFT -> GuiComponent.blit(matrix, posX + width - filledWidth, posY, width - filledWidth, 0, filledWidth, height, width, height);
                case TOP -> GuiComponent.blit(matrix, posX, posY, 0, 0, width, filledHeight, width, height);
                case BOTTOM -> GuiComponent.blit(matrix, posX, posY + height - filledHeight, 0, height - filledHeight, width, filledHeight, width, height);
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
    public List<Component> getJEITooltips(ProgressBarGuiElement element, IMachineRecipe recipe) {
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(new TranslatableComponent("custommachinery.jei.recipe.time", recipe.getRecipeTime()));
        if(!CMConfig.INSTANCE.needAdvancedInfoForRecipeID.get() || Minecraft.getInstance().options.advancedItemTooltips)
            tooltips.add(new TranslatableComponent("custommachinery.jei.recipe.id", recipe.getRecipeId().toString()).withStyle(ChatFormatting.DARK_GRAY));
        return tooltips;
    }

    private void rotate(PoseStack matrix, ProgressBarGuiElement.Direction direction, int posX, int posY, int width, int height) {
        switch (direction) {
            case RIGHT:
                matrix.translate(posX, posY, 0);
                break;
            case LEFT:
                matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
                matrix.translate(-width - posX, -height - posY, 0);
                break;
            case TOP:
                matrix.mulPose(Vector3f.ZP.rotationDegrees(270));
                matrix.translate(-width - posY, posX, 0);
                break;
            case BOTTOM:
                matrix.mulPose(Vector3f.ZP.rotationDegrees(90));
                matrix.translate(posY, -height - posX, 0);
                break;
        }
    }
}
