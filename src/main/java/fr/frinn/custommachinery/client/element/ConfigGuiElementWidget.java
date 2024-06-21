package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.MachineConfigScreen;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ConfigGuiElementWidget extends AbstractGuiElementWidget<ConfigGuiElement> {

    private static final Component TITLE = Component.translatable("custommachinery.gui.element.config.name");

    public ConfigGuiElementWidget(ConfigGuiElement element, IMachineScreen screen) {
        super(element, screen, TITLE);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int i, int j, float f) {
        if(!this.isHovered())
            graphics.blit(this.getElement().getTexture(), this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        else
            graphics.blit(this.getElement().getTextureHovered(), this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Minecraft.getInstance().setScreen(new MachineConfigScreen((CustomMachineScreen) this.getScreen()));
    }
}
