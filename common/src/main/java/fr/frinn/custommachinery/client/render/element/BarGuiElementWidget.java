package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.BarGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

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
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        int filledWidth = (int)(this.width * Mth.clamp(getFillingPercent(), 0.0D, 1.0D));
        int filledHeight = (int)(this.height * Mth.clamp(getFillingPercent(), 0.0D, 1.0D));

        ClientHandler.bindTexture(this.getElement().getEmptyTexture());
        GuiComponent.blit(pose, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        ClientHandler.bindTexture(this.getElement().getFilledTexture());
        switch (this.getElement().getOrientation()) {
            case RIGHT -> GuiComponent.blit(pose, this.x, this.y, 0, 0, filledWidth, this.height, this.width, this.height);
            case LEFT -> GuiComponent.blit(pose, this.x + width - filledWidth, this.y, this.width - filledWidth, 0, filledWidth, this.height, this.width, this.height);
            case BOTTOM -> GuiComponent.blit(pose, this.x, this.y, 0, 0, this.width, filledHeight, this.width, this.height);
            case TOP -> GuiComponent.blit(pose, this.x, this.y + this.height - filledHeight, 0, this.height - filledHeight, this.width, filledHeight, this.width, this.height);
        }
        if(this.isHoveredOrFocused() && this.getElement().isHighlight())
            ClientHandler.renderSlotHighlight(pose, this.x + 1, this.y + 1, this.width - 2, this.height - 2);
    }

    private double getFillingPercent() {
        double amount = this.getScreen().getTile().getComponentManager().getComponent(Registration.DATA_MACHINE_COMPONENT.get())
                .map(component -> component.getData().getDouble(this.getElement().getId()))
                .orElse(0.0D);

        return (amount - this.getElement().getMin()) / this.getElement().getMax();
    }
}
