package fr.frinn.custommachinery.common.guielement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.resources.ResourceLocation;

public class TextureGuiElement extends AbstractTexturedGuiElement {

    public static final Codec<TextureGuiElement> CODEC = RecordCodecBuilder.create(textureGuiElement ->
            makeBaseCodec(textureGuiElement).and(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(AbstractTexturedGuiElement::getTexture)
            ).apply(textureGuiElement, TextureGuiElement::new)
    );

    public TextureGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority, texture);
    }

    @Override
    public GuiElementType<TextureGuiElement> getType() {
        return Registration.TEXTURE_GUI_ELEMENT.get();
    }
}
