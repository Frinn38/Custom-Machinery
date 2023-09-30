package fr.frinn.custommachinery.client.integration.jei.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.render.element.ProgressGuiElementWidget;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.integration.config.CMConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class ProgressGuiElementJeiRenderer implements IJEIElementRenderer<ProgressBarGuiElement> {

    @Override
    public void renderElementInJEI(PoseStack matrix, ProgressBarGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
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

        ClientHandler.bindTexture(element.getEmptyTexture());

        if(element.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && element.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE)) {
            matrix.pushPose();
            ProgressGuiElementWidget.rotate(matrix, element.getDirection(), posX, posY, width, height);

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
