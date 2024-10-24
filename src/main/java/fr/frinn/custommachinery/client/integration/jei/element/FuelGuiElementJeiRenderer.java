package fr.frinn.custommachinery.client.integration.jei.element;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.FuelGuiElement;
import fr.frinn.custommachinery.common.requirement.FuelRequirement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class FuelGuiElementJeiRenderer implements IJEIElementRenderer<FuelGuiElement> {

    @Override
    public void renderElementInJEI(GuiGraphics graphics, FuelGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        double percent = 1 - (System.currentTimeMillis() % 2000) / 2000.0D;
        ClientHandler.renderOrientedProgressTextures(graphics, element.getEmptyTexture(), element.getFilledTexture(), element.getX(), element.getY(), element.getWidth(), element.getHeight(), percent, element.getOrientation());
    }

    @Override
    public List<Component> getJEITooltips(FuelGuiElement element, IMachineRecipe recipe) {
        int amount = recipe.getRequirements().stream().filter(requirement -> requirement.requirement() instanceof FuelRequirement).findFirst().map(requirement -> ((FuelRequirement)requirement.requirement()).amount()).orElse(0);
        if(amount > 0)
            return Lists.newArrayList(Component.translatable("custommachinery.jei.ingredient.fuel.amount", amount));
        return Collections.emptyList();
    }
}
