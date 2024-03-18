package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.render.FluidRenderer;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class FluidGuiElementWidget extends TexturedGuiElementWidget<FluidGuiElement> {

    public FluidGuiElementWidget(FluidGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Fluid"));
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        this.getScreen().getTile().getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(fluidHandler -> fluidHandler.getComponentForID(this.getElement().getComponentId())).ifPresent(component -> {
            FluidRenderer.renderFluid(poseStack, this.x + 1, this.y + 1, this.width - 2, this.height - 2, component.getFluidStack(), component.getCapacity());
        });
        if(this.isHoveredOrFocused() && this.getElement().highlight())
            ClientHandler.renderSlotHighlight(poseStack, this.x + 1, this.y + 1, this.width - 2, this.height - 2);
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
                    long amount = component.getFluidStack().getAmount() * 1000L / FluidStackHooks.bucketAmount();
                    long capacity = component.getCapacity() * 1000L / FluidStackHooks.bucketAmount();
                    if(!component.getFluidStack().isEmpty() && amount > 0)
                        tooltip = Component.translatable(component.getFluidStack().getTranslationKey()).append(Component.translatable("custommachinery.gui.element.fluid.tooltip", Utils.format(amount), Utils.format(capacity)));
                    else
                        tooltip = Component.translatable("custommachinery.gui.element.fluid.empty", 0, Utils.format(capacity));
                    return Collections.singletonList(tooltip);
                })
                .orElse(Collections.emptyList());
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}
