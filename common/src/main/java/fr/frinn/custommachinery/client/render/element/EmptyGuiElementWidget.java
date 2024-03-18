package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.EmptyGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.network.chat.Component;

public class EmptyGuiElementWidget extends AbstractGuiElementWidget<EmptyGuiElement> {
    public EmptyGuiElementWidget(EmptyGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.empty());
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {

    }
}
