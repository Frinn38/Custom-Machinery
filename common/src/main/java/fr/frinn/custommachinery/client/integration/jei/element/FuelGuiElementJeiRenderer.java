package fr.frinn.custommachinery.client.integration.jei.element;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.common.guielement.FuelGuiElement;
import fr.frinn.custommachinery.common.requirement.FuelRequirement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class FuelGuiElementJeiRenderer implements IJEIElementRenderer<FuelGuiElement> {

    @Override
    public void renderElementInJEI(GuiGraphics graphics, FuelGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        if(Minecraft.getInstance().level == null)
            return;
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        graphics.blit(element.getEmptyTexture(), posX, posY, 0, 0, width, height, width, height);
        int filledHeight = (int)(Minecraft.getInstance().level.getGameTime() / 2 % height);
        graphics.blit(element.getFilledTexture(), posX, posY + height - filledHeight, 0, height - filledHeight, width, filledHeight, width, height);
    }

    @Override
    public List<Component> getJEITooltips(FuelGuiElement element, IMachineRecipe recipe) {
        int amount = recipe.getRequirements().stream().filter(requirement -> requirement instanceof FuelRequirement).findFirst().map(requirement -> ((FuelRequirement)requirement).getAmount()).orElse(0);
        if(amount > 0)
            return Lists.newArrayList(Component.translatable("custommachinery.jei.ingredient.fuel.amount", amount));
        return Collections.emptyList();
    }
}
