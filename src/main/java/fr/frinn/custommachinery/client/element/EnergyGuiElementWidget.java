package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class EnergyGuiElementWidget extends TexturedGuiElementWidget<EnergyGuiElement> {

    public EnergyGuiElementWidget(EnergyGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Energy"));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        double percent = this.getScreen().getTile()
                        .getComponentManager()
                        .getComponent(Registration.ENERGY_MACHINE_COMPONENT.get())
                        .map(EnergyMachineComponent::getFillPercent)
                        .orElse(0.0D);
        ClientHandler.renderOrientedProgressTextures(graphics, this.getElement().getEmptyTexture(), this.getElement().getFilledTexture(), this.getX(), this.getY(), this.width, this.height, percent, this.getElement().getOrientation());
        if(this.isHovered() && this.getElement().highlight())
            ClientHandler.renderSlotHighlight(graphics, this.getX() + 1, this.getY() + 1, this.width - 2, this.height - 2);
    }

    @Override
    public List<Component> getTooltips() {
        if(!this.getElement().getTooltips().isEmpty())
            return this.getElement().getTooltips();
        return this.getScreen().getTile().getComponentManager()
                .getComponent(Registration.ENERGY_MACHINE_COMPONENT.get())
                .map(component -> Collections.singletonList((Component)
                        Component.translatable(
                                "custommachinery.gui.element.energy.tooltip",
                                Utils.format(component.getEnergy()),
                                Component.translatable("unit.energy.forge"),
                                Utils.format(component.getCapacity()),
                                Component.translatable("unit.energy.forge")
                        )
                ))
                .orElse(Collections.emptyList());
    }
}
