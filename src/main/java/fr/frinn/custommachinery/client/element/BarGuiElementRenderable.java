package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.BarGuiElement;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.client.AbstractGuiElementRenderable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BarGuiElementRenderable extends AbstractGuiElementRenderable<BarGuiElement> {

    public BarGuiElementRenderable(BarGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }

    @Override
    public List<Component> getTooltips() {
        List<Component> tooltips = new ArrayList<>(this.getElement().getTooltips());
        this.getScreen().getTile().getComponentManager()
                .getComponent(CMRegistration.DATA_MACHINE_COMPONENT.get())
                .ifPresent(component -> tooltips.add(Component.literal(String.format("%s / %s", component.getData().getDouble(this.getElement().getId()), this.getElement().getMax()))));
        return tooltips;
    }

    @Override
    public void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ClientHandler.renderOrientedProgressTextures(graphics, this.getElement().getEmptyTexture(), this.getElement().getFilledTexture(), this.getElement().getX(), this.getElement().getY(), this.getElement().getWidth(), this.getElement().getHeight(), this.getFillingPercent(), this.getElement().getOrientation());
        if(this.isHovered(mouseX, mouseY) && this.getElement().isHighlight())
            ClientHandler.renderSlotHighlight(graphics, this.getElement().getX() + 1, this.getElement().getY() + 1, this.getElement().getWidth() - 2, this.getElement().getHeight() - 2);
    }

    private double getFillingPercent() {
        double amount = this.getScreen().getTile().getComponentManager().getComponent(CMRegistration.DATA_MACHINE_COMPONENT.get())
                .map(component -> component.getData().getDoubleOr(this.getElement().getId(), 0))
                .orElse(0.0D);

        return (amount - this.getElement().getMin()) / this.getElement().getMax();
    }
}
