package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.PlatformHelper;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collections;
import java.util.List;

public class EnergyGuiElementWidget extends TexturedGuiElementWidget<EnergyGuiElement> {

    public EnergyGuiElementWidget(EnergyGuiElement element, IMachineScreen screen) {
        super(element, screen, new TextComponent("Energy"));
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        this.getScreen().getTile().getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get()).ifPresent(energy -> {
            double fillPercent = energy.getFillPercent();
            int eneryHeight = (int)(fillPercent * (double)(this.height));
            ClientHandler.bindTexture(this.getElement().getFilledTexture());
            GuiComponent.blit(poseStack, this.x, this.y + this.height - eneryHeight, 0, this.height - eneryHeight, this.width, eneryHeight, this.width, this.height);
        });
    }

    @Override
    public List<Component> getTooltips() {
        return this.getScreen().getTile().getComponentManager()
                .getComponent(Registration.ENERGY_MACHINE_COMPONENT.get())
                .map(component -> Collections.singletonList((Component)
                        new TranslatableComponent(
                            "custommachinery.gui.element.energy.tooltip",
                                Utils.format(component.getEnergy()) + " " + PlatformHelper.energy().unit(),
                            Utils.format(component.getCapacity()) + " " + PlatformHelper.energy().unit()
                        )
                ))
                .orElse(Collections.emptyList());
    }
}
