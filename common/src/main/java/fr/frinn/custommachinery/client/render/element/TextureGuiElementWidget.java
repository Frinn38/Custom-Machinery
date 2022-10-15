package fr.frinn.custommachinery.client.render.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.TextureGuiElement;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.network.chat.TextComponent;

public class TextureGuiElementWidget extends TexturedGuiElementWidget<TextureGuiElement> {

    public TextureGuiElementWidget(TextureGuiElement element, IMachineScreen screen) {
        super(element, screen, new TextComponent("Texture"));
    }

    @Override
    public boolean clicked(double d, double e) {
        return false;
    }
}
