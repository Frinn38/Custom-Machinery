package fr.frinn.custommachinery.client.render.element;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.component.FuelMachineComponent;
import fr.frinn.custommachinery.common.guielement.FuelGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.requirement.FuelRequirement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collections;
import java.util.List;

public class FuelGuiElementRenderer implements IGuiElementRenderer<FuelGuiElement>, IJEIElementRenderer<FuelGuiElement> {

    @Override
    public void renderElement(PoseStack matrix, FuelGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        ClientHandler.bindTexture(element.getEmptyTexture());
        GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        int fuel = screen.getTile().getComponentManager().getComponent(Registration.FUEL_MACHINE_COMPONENT.get()).map(FuelMachineComponent::getFuel).orElse(0);
        int maxFuel = screen.getTile().getComponentManager().getComponent(Registration.FUEL_MACHINE_COMPONENT.get()).map(FuelMachineComponent::getMaxFuel).orElse(0);
        if(fuel != 0 && maxFuel != 0) {
            double filledPercent = (double)fuel / (double)maxFuel;
            ClientHandler.bindTexture(element.getFilledTexture());
            GuiComponent.blit(matrix, posX, posY + (int)(height *  (1 - filledPercent)), 0, (int)(height * (1 - filledPercent)), width, (int)(height * filledPercent), width, height);
        }
    }

    @Override
    public List<Component> getTooltips(FuelGuiElement element, IMachineScreen screen) {
        return screen.getTile().getComponentManager()
                .getComponent(Registration.FUEL_MACHINE_COMPONENT.get())
                .map(component -> Collections.singletonList((Component)new TranslatableComponent("custommachinery.gui.element.fuel.tooltip", component.getFuel())))
                .orElse(Collections.emptyList());
    }

    @Override
    public void renderElementInJEI(PoseStack matrix, FuelGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        if(Minecraft.getInstance().level == null)
            return;
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        ClientHandler.bindTexture(element.getEmptyTexture());
        GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        int filledHeight = (int)(Minecraft.getInstance().level.getGameTime() / 2 % height);
        ClientHandler.bindTexture(element.getFilledTexture());
        GuiComponent.blit(matrix, posX, posY + height - filledHeight, 0, height - filledHeight, width, filledHeight, width, height);
    }

    @Override
    public List<Component> getJEITooltips(FuelGuiElement element, IMachineRecipe recipe) {
        int amount = recipe.getRequirements().stream().filter(requirement -> requirement instanceof FuelRequirement).findFirst().map(requirement -> ((FuelRequirement)requirement).getAmount()).orElse(0);
        if(amount > 0)
            return Lists.newArrayList(new TranslatableComponent("custommachinery.jei.ingredient.fuel.amount", amount));
        return Collections.emptyList();
    }
}
