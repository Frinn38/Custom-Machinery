package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.PlatformHelper;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
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
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
        this.getScreen().getTile().getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).ifPresent(energy -> {
            double fillPercent = energy.getFillPercent();
            int eneryHeight = (int)(fillPercent * (double)(this.height));
            graphics.blit(this.getElement().getFilledTexture(), this.getX(), this.getY() + this.height - eneryHeight, 0, this.height - eneryHeight, this.width, eneryHeight, this.width, this.height);
        });
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
                                PlatformHelper.energy().unit(),
                                Utils.format(component.getCapacity()),
                                PlatformHelper.energy().unit()
                        )
                ))
                .orElse(Collections.emptyList());
    }
}
