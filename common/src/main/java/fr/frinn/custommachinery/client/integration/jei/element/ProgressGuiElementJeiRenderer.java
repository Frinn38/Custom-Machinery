package fr.frinn.custommachinery.client.integration.jei.element;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.element.ProgressGuiElementWidget;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.integration.config.CMConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class ProgressGuiElementJeiRenderer implements IJEIElementRenderer<ProgressBarGuiElement> {

    @Override
    public void renderElementInJEI(GuiGraphics graphics, ProgressBarGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        if(Minecraft.getInstance().level == null)
            return;

        int filledWidth = 0;
        int filledHeight = 0;
        if(recipe.getRecipeTime() > 0) {
            filledWidth = (int)(width * Mth.clamp(Mth.map((float) (Minecraft.getInstance().level.getGameTime() % recipe.getRecipeTime()) / recipe.getRecipeTime(), element.getStart(), element.getEnd(), 0, 1), 0.0D, 1.0D));
            filledHeight = (int)(height * Mth.clamp(Mth.map((float) (Minecraft.getInstance().level.getGameTime() % recipe.getRecipeTime()) / recipe.getRecipeTime(), element.getStart(), element.getEnd(), 0, 1), 0.0D, 1.0D));
        }

        if(element.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && element.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE)) {
            graphics.pose().pushPose();
            ProgressGuiElementWidget.rotate(graphics.pose(), element.getDirection(), posX, posY, width, height);

            graphics.blit(element.getEmptyTexture(), 0, 0, 0, 0, width, height, width, height);
            graphics.blit(element.getFilledTexture(), 0, 0, 0, 0, filledWidth, height, width, height);

            graphics.pose().popPose();
        } else {
            graphics.blit(element.getEmptyTexture(), posX, posY, 0, 0, width, height, width, height);
            switch (element.getDirection()) {
                case RIGHT -> graphics.blit(element.getFilledTexture(), posX, posY, 0, 0, filledWidth, height, width, height);
                case LEFT -> graphics.blit(element.getFilledTexture(), posX + width - filledWidth, posY, width - filledWidth, 0, filledWidth, height, width, height);
                case TOP -> graphics.blit(element.getFilledTexture(), posX, posY, 0, 0, width, filledHeight, width, height);
                case BOTTOM -> graphics.blit(element.getFilledTexture(), posX, posY + height - filledHeight, 0, height - filledHeight, width, filledHeight, width, height);
            }
        }
    }

    @Override
    public boolean isHoveredInJei(ProgressBarGuiElement element, int posX, int posY, int mouseX, int mouseY) {
        boolean invertAxis = element.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && element.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE) && element.getDirection() != ProgressBarGuiElement.Orientation.RIGHT && element.getDirection() != ProgressBarGuiElement.Orientation.LEFT;
        int width = invertAxis ? element.getHeight() : element.getWidth();
        int height = invertAxis ? element.getWidth() : element.getHeight();
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }

    @Override
    public List<Component> getJEITooltips(ProgressBarGuiElement element, IMachineRecipe recipe) {
        List<Component> tooltips = new ArrayList<>();
        if(recipe.getRecipeTime() > 0)
            tooltips.add(Component.translatable("custommachinery.jei.recipe.time", recipe.getRecipeTime()));
        else
            tooltips.add(Component.translatable("custommachinery.jei.recipe.instant"));
        if(!CMConfig.get().needAdvancedInfoForRecipeID || Minecraft.getInstance().options.advancedItemTooltips)
            tooltips.add(Component.translatable("custommachinery.jei.recipe.id", recipe.getRecipeId().toString()).withStyle(ChatFormatting.DARK_GRAY));
        return tooltips;
    }
}
