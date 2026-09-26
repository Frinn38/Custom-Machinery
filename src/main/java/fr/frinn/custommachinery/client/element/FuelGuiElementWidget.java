package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.FuelGuiElement;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class FuelGuiElementWidget extends AbstractGuiElementWidget<FuelGuiElement> {


    public FuelGuiElementWidget(FuelGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Fuel"));
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        double percent = this.getScreen().getTile()
                .getComponentManager()
                .getComponent(CMRegistration.FUEL_MACHINE_COMPONENT.get())
                .map(component -> component.getMaxFuel() == 0 ? 0.0D : component.getFuel() / (double)component.getMaxFuel())
                .orElse(0.0D);
        if(percent == 0 && this.getScreen().getMachine().isDummy())
            percent = 1 - (System.currentTimeMillis() % 2000) / 2000.0D;
        ClientHandler.renderOrientedProgressTextures(graphics, this.getElement().getEmptyTexture(), this.getElement().getFilledTexture(), this.getX(), this.getY(), this.width, this.height, percent, this.getElement().getOrientation());
    }

    @Override
    public List<Component> getTooltips() {
        if(!this.getElement().getTooltips().isEmpty())
            return this.getElement().getTooltips();
        return this.getScreen().getTile().getComponentManager()
                .getComponent(CMRegistration.FUEL_MACHINE_COMPONENT.get())
                .map(component -> Collections.singletonList((Component)Component.translatable("custommachinery.gui.element.fuel.tooltip", component.getFuel())))
                .orElse(Collections.emptyList());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return false;
    }
}
