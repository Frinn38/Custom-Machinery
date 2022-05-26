package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.render.FluidRenderer;
import fr.frinn.custommachinery.common.data.gui.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collections;
import java.util.List;

public class FluidGuiElementRenderer implements IGuiElementRenderer<FluidGuiElement>, IJEIElementRenderer<FluidGuiElement> {

    @Override
    public void renderElement(PoseStack matrix, FluidGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        ClientHandler.bindTexture(element.getTexture());
        GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
        screen.getTile().getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(fluidHandler -> fluidHandler.getComponentForID(element.getID())).ifPresent(component -> {
            FluidRenderer.renderFluid(matrix, posX + 1, posY + 1, width - 2, height - 2, component.getFluidStack(), component.getCapacity());
        });
    }

    @Override
    public void renderTooltip(PoseStack matrix, FluidGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        screen.getTile().getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(fluidHandler -> fluidHandler.getComponentForID(element.getID())).ifPresent(component -> {
            String fluid = component.getFluidStack().getTranslationKey();
            int amount = component.getFluidStack().getAmount();
            int capacity = component.getCapacity();
            screen.getScreen().renderTooltip(matrix, new TranslatableComponent(fluid).append(new TranslatableComponent("custommachinery.gui.element.fluid.tooltip", amount, capacity)), mouseX, mouseY);
        });
    }

    @Override
    public void renderElementInJEI(PoseStack matrix, FluidGuiElement element, IMachineRecipe recipe, int mouseX, int mouseY) {
        int posX = element.getX() - 1;
        int posY = element.getY() - 1;
        int width = element.getWidth();
        int height = element.getHeight();
        ClientHandler.bindTexture(element.getTexture());
        GuiComponent.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }

    @Override
    public List<Component> getJEITooltips(FluidGuiElement element, IMachineRecipe recipe) {
        return Collections.emptyList();
    }
}
