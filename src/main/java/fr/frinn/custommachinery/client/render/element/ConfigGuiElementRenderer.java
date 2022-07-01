package fr.frinn.custommachinery.client.render.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.MachineConfigScreen;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class ConfigGuiElementRenderer extends TexturedGuiElementRenderer<ConfigGuiElement> {

    private static final List<Component> TOOLTIPS = List.of(new TranslatableComponent("custommachinery.gui.element.config.name"));

    @Override
    public List<Component> getTooltips(ConfigGuiElement element, IMachineScreen screen) {
        return TOOLTIPS;
    }

    @Override
    public void handleClick(ConfigGuiElement element, IMachineScreen screen, int mouseX, int mouseY, int button) {
        Minecraft.getInstance().pushGuiLayer(new MachineConfigScreen((CustomMachineScreen) screen));
    }
}
