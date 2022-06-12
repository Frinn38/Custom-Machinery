package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class ConfigGuiElementRenderer extends TexturedGuiElementRenderer<ConfigGuiElement> {

    private static final List<Component> TOOLTIPS = List.of(new TranslatableComponent("custommachinery.gui.element.config.name"));

    @Override
    public void renderTooltip(PoseStack matrix, ConfigGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        screen.getScreen().renderComponentTooltip(matrix, TOOLTIPS, mouseX, mouseY);
    }
}
