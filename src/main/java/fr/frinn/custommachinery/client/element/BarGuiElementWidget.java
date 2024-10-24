package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.BarGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BarGuiElementWidget extends AbstractGuiElementWidget<BarGuiElement> {

    private static final Component TITLE = Component.literal("Bar");

    public BarGuiElementWidget(BarGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);
    }

    @Override
    public List<Component> getTooltips() {
        List<Component> tooltips = new ArrayList<>(this.getElement().getTooltips());
        this.getScreen().getTile().getComponentManager()
                .getComponent(Registration.DATA_MACHINE_COMPONENT.get())
                .ifPresent(component -> tooltips.add(Component.literal(String.format("%s / %s", component.getData().getDouble(this.getElement().getId()), this.getElement().getMax()))));
        return tooltips;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientHandler.renderOrientedProgressTextures(graphics, this.getElement().getEmptyTexture(), this.getElement().getFilledTexture(), this.getX(), this.getY(), this.width, this.height, this.getFillingPercent(), this.getElement().getOrientation());
        if(this.isHovered() && this.getElement().isHighlight())
            ClientHandler.renderSlotHighlight(graphics, this.getX() + 1, this.getY() + 1, this.width - 2, this.height - 2);
    }

    private double getFillingPercent() {
        double amount = this.getScreen().getTile().getComponentManager().getComponent(Registration.DATA_MACHINE_COMPONENT.get())
                .map(component -> component.getData().getDouble(this.getElement().getId()))
                .orElse(0.0D);

        return (amount - this.getElement().getMin()) / this.getElement().getMax();
    }
}
