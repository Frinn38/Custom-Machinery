package fr.frinn.custommachinery.client.render.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class ConfigGuiElementRenderer extends TexturedGuiElementRenderer<ConfigGuiElement> {

    private static final List<Component> TOOLTIPS = List.of(new TranslatableComponent("custommachinery.gui.element.config.name"));

    @Override
    public List<Component> getTooltips(ConfigGuiElement element, IMachineScreen screen) {
        return TOOLTIPS;
    }
}
