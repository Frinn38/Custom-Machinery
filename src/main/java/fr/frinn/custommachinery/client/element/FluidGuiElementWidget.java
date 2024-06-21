package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.render.FluidRenderer;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class FluidGuiElementWidget extends TexturedGuiElementWidget<FluidGuiElement> {

    public FluidGuiElementWidget(FluidGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Fluid"));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
        this.getScreen().getTile().getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(fluidHandler -> fluidHandler.getComponentForID(this.getElement().getComponentId())).ifPresent(component -> {
            FluidRenderer.renderFluid(graphics.pose(), this.getX() + 1, this.getY() + 1, this.width - 2, this.height - 2, component.getFluidStack(), component.getCapacity());
        });
        if(this.isHovered() && this.getElement().highlight())
            ClientHandler.renderSlotHighlight(graphics, this.getX() + 1, this.getY() + 1, this.width - 2, this.height - 2);
    }

    @Override
    public List<Component> getTooltips() {
        if(!this.getElement().getTooltips().isEmpty())
            return this.getElement().getTooltips();
        return this.getScreen().getTile().getComponentManager()
                .getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(fluidHandler -> fluidHandler.getComponentForID(this.getElement().getComponentId()))
                .map(component -> {
                    Component tooltip;
                    long amount = component.getFluidStack().getAmount();
                    long capacity = component.getCapacity();
                    if(!component.getFluidStack().isEmpty() && amount > 0)
                        tooltip = Component.empty().append(component.getFluidStack().getHoverName()).append(Component.translatable("custommachinery.gui.element.fluid.tooltip", Utils.format(amount), Utils.format(capacity)));
                    else
                        tooltip = Component.translatable("custommachinery.gui.element.fluid.empty", 0, Utils.format(capacity));
                    return Collections.singletonList(tooltip);
                })
                .orElse(Collections.emptyList());
    }
}
