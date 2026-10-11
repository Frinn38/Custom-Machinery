package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.client.TexturedGuiElementRenderable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class EnergyGuiElementWidget extends TexturedGuiElementRenderable<EnergyGuiElement> {

    public EnergyGuiElementWidget(EnergyGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }

    @Override
    public void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        double percent = this.getScreen().getTile()
                        .getComponentManager()
                        .getComponent(CMRegistration.ENERGY_MACHINE_COMPONENT.get())
                        .map(EnergyMachineComponent::getFillPercent)
                        .orElse(0.0D);
        ClientHandler.renderOrientedProgressTextures(graphics, this.getElement().getEmptyTexture(), this.getElement().getFilledTexture(), this.getElement().getX(), this.getElement().getY(), this.getElement().getWidth(), this.getElement().getHeight(), percent, this.getElement().getOrientation());
        if(this.isHovered(mouseX, mouseY) && this.getElement().highlight())
            ClientHandler.renderSlotHighlight(graphics, this.getElement().getX() + 1, this.getElement().getY() + 1, this.getElement().getWidth() - 2, this.getElement().getHeight() - 2);
    }

    @Override
    public List<Component> getTooltips() {
        if(!this.getElement().getTooltips().isEmpty())
            return this.getElement().getTooltips();
        return this.getScreen().getTile().getComponentManager()
                .getComponent(CMRegistration.ENERGY_MACHINE_COMPONENT.get())
                .map(component -> Collections.singletonList((Component)
                        Component.translatable(
                                "custommachinery.gui.element.energy.tooltip",
                                Utils.format(component.getEnergy()),
                                Component.translatable("unit.energy.forge"),
                                Utils.format(component.getClientCapacity()),
                                Component.translatable("unit.energy.forge")
                        )
                ))
                .orElse(Collections.emptyList());
    }
}
