package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.client.screen.ScreenUtils;
import fr.frinn.custommachinery.common.guielement.FuelGuiElement;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.client.AbstractGuiElementRenderable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class FuelGuiElementWidget extends AbstractGuiElementRenderable<FuelGuiElement> {


    public FuelGuiElementWidget(FuelGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }

    @Override
    public void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        double percent = this.getScreen().getTile()
                .getComponentManager()
                .getComponent(CMRegistration.FUEL_MACHINE_COMPONENT.get())
                .map(component -> component.getMaxFuel() == 0 ? 0.0D : component.getFuel() / (double)component.getMaxFuel())
                .orElse(0.0D);
        if(percent == 0 && this.getScreen().getMachine().isDummy())
            percent = 1 - (System.currentTimeMillis() % 2000) / 2000.0D;
        ScreenUtils.renderOrientedProgressTextures(graphics, this.getElement().getEmptyTexture(), this.getElement().getFilledTexture(), this.getElement().getX(), this.getElement().getY(), this.getElement().getWidth(), this.getElement().getHeight(), percent, this.getElement().getOrientation());
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
}
