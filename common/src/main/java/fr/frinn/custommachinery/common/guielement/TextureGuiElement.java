package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

public class TextureGuiElement extends AbstractTexturedGuiElement {

    public static final NamedCodec<TextureGuiElement> CODEC = NamedCodec.record(textureGuiElement ->
            makeBaseCodec(textureGuiElement).and(
                    DefaultCodecs.RESOURCE_LOCATION.fieldOf("texture").forGetter(AbstractTexturedGuiElement::getTexture)
            ).apply(textureGuiElement, TextureGuiElement::new), "Texture gui element"
    );

    public TextureGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority, texture);
    }

    @Override
    public GuiElementType<TextureGuiElement> getType() {
        return Registration.TEXTURE_GUI_ELEMENT.get();
    }
}
