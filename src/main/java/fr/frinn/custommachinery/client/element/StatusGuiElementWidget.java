package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.guielement.StatusGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatusGuiElementWidget extends AbstractGuiElementWidget<StatusGuiElement> {

    public StatusGuiElementWidget(StatusGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Status"));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        ResourceLocation texture;
        switch (this.getScreen().getTile().getStatus()) {
            case RUNNING -> texture = this.getElement().getRunningTexture();
            case ERRORED -> texture = this.getElement().getErroredTexture();
            default -> texture = this.getElement().getIdleTexture();
        }
        graphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    public List<Component> getTooltips() {
        if(!this.getElement().getTooltips().isEmpty())
            return this.getElement().getTooltips();
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(Component.translatable("custommachinery.craftingstatus." + this.getScreen().getTile().getStatus().toString().toLowerCase(Locale.ENGLISH)));
        if(this.getScreen().getTile().getStatus() == MachineStatus.ERRORED)
            tooltips.add(this.getScreen().getTile().getMessage());
        return tooltips;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return false;
    }
}
