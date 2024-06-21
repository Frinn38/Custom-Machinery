package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.TextureGuiElement;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.network.chat.Component;

public class TextureGuiElementWidget extends TexturedGuiElementWidget<TextureGuiElement> {

    public TextureGuiElementWidget(TextureGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Texture"));
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return false;
    }
}
