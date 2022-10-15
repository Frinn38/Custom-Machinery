package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.render.FluidRenderer;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collections;
import java.util.List;

public class FluidGuiElementWidget extends TexturedGuiElementWidget<FluidGuiElement> {

    public FluidGuiElementWidget(FluidGuiElement element, IMachineScreen screen) {
        super(element, screen, new TextComponent("Fluid"));
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        this.getScreen().getTile().getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(fluidHandler -> fluidHandler.getComponentForID(this.getElement().getID())).ifPresent(component -> {
            FluidRenderer.renderFluid(poseStack, this.x + 1, this.y + 1, width - 2, height - 2, component.getFluidStack(), component.getCapacity());
        });
    }

    @Override
    public List<Component> getTooltips() {
        return this.getScreen().getTile().getComponentManager()
                .getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                .flatMap(fluidHandler -> fluidHandler.getComponentForID(this.getElement().getID()))
                .map(component -> {
                    Component tooltip;
                    long amount = component.getFluidStack().getAmount() * 1000L / FluidStackHooks.bucketAmount();
                    long capacity = component.getCapacity() * 1000L / FluidStackHooks.bucketAmount();
                    if(!component.getFluidStack().isEmpty() && amount > 0)
                        tooltip = new TranslatableComponent(component.getFluidStack().getTranslationKey()).append(new TranslatableComponent("custommachinery.gui.element.fluid.tooltip", Utils.format(amount), Utils.format(capacity)));
                    else
                        tooltip = new TranslatableComponent("custommachinery.gui.element.fluid.empty", 0, Utils.format(capacity));
                    return Collections.singletonList(tooltip);
                })
                .orElse(Collections.emptyList());
    }
}
