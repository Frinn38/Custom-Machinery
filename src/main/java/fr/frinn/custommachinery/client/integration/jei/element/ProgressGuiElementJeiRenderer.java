package fr.frinn.custommachinery.client.integration.jei.element;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.element.ProgressGuiElementWidget;
import fr.frinn.custommachinery.client.render.ProgressArrowRenderer;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class ProgressGuiElementJeiRenderer implements IJEIElementRenderer<ProgressBarGuiElement> {

    @Override
    public void renderElementInJEI(GuiGraphics graphics, ProgressBarGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        if(Minecraft.getInstance().level == null)
            return;
        ProgressArrowRenderer.renderProgressArrow(graphics, element, element.getX(), element.getY(), recipe.getRecipeTime() <= 0 ? 0 : (double) (Minecraft.getInstance().level.getGameTime() % recipe.getRecipeTime()) / recipe.getRecipeTime());
    }

    @Override
    public List<Component> getJEITooltips(ProgressBarGuiElement element, IMachineRecipe recipe) {
        List<Component> tooltips = new ArrayList<>();
        if(recipe.getRecipeTime() > 0)
            tooltips.add(Component.translatable("custommachinery.jei.recipe.time", recipe.getRecipeTime()));
        else
            tooltips.add(Component.translatable("custommachinery.jei.recipe.instant"));
        //if(!CMConfig.get().needAdvancedInfoForRecipeID || Minecraft.getInstance().options.advancedItemTooltips)
        //    tooltips.add(Component.translatable("custommachinery.jei.recipe.id", recipe.getRecipeId().toString()).withStyle(ChatFormatting.DARK_GRAY));
        return tooltips;
    }
}
