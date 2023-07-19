package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.component.FuelMachineComponent;
import fr.frinn.custommachinery.common.guielement.FuelGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class FuelGuiElementWidget extends AbstractGuiElementWidget<FuelGuiElement> {


    public FuelGuiElementWidget(FuelGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Fuel"));
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        ClientHandler.bindTexture(this.getElement().getEmptyTexture());
        GuiComponent.blit(poseStack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        int fuel = this.getScreen().getTile().getComponentManager().getComponent(Registration.FUEL_MACHINE_COMPONENT.get()).map(FuelMachineComponent::getFuel).orElse(0);
        int maxFuel = this.getScreen().getTile().getComponentManager().getComponent(Registration.FUEL_MACHINE_COMPONENT.get()).map(FuelMachineComponent::getMaxFuel).orElse(0);
        if(fuel != 0 && maxFuel != 0) {
            double filledPercent = (double)fuel / (double)maxFuel;
            ClientHandler.bindTexture(this.getElement().getFilledTexture());
            GuiComponent.blit(poseStack, this.x, this.y + (int)(height *  (1 - filledPercent)), 0, (int)(height * (1 - filledPercent)), width, (int)(height * filledPercent), width, height);
        }
    }

    @Override
    public List<Component> getTooltips() {
        if(!this.getElement().getTooltips().isEmpty())
            return this.getElement().getTooltips();
        return this.getScreen().getTile().getComponentManager()
                .getComponent(Registration.FUEL_MACHINE_COMPONENT.get())
                .map(component -> Collections.singletonList((Component)Component.translatable("custommachinery.gui.element.fuel.tooltip", component.getFuel())))
                .orElse(Collections.emptyList());
    }
}
