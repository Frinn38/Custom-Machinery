package fr.frinn.custommachinery.client.render.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.TextureGuiElement;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class TextureGuiElementRenderer extends TexturedGuiElementRenderer<TextureGuiElement> {

    @Override
    public List<Component> getTooltips(TextureGuiElement element, IMachineScreen screen) {
        return Collections.emptyList();
    }

    @Override
    public boolean isHovered(TextureGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        return false;
    }
}
