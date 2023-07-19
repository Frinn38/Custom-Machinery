package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.MachineConfigScreen;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class ConfigGuiElementWidget extends AbstractGuiElementWidget<ConfigGuiElement> {

    private static final Component TITLE = Component.translatable("custommachinery.gui.element.config.name");

    public ConfigGuiElementWidget(ConfigGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        if(!isHoveredOrFocused())
            RenderSystem.setShaderTexture(0, getElement().getTexture());
        else
            RenderSystem.setShaderTexture(0, getElement().getTextureHovered());
        GuiComponent.blit(poseStack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public void onClick(double d, double e) {
        Minecraft.getInstance().setScreen(new MachineConfigScreen((CustomMachineScreen) this.getScreen()));
    }
}
