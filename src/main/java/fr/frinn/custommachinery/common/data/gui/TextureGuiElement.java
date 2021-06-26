package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

public class TextureGuiElement extends TexturedGuiElement {

    public static final Codec<TextureGuiElement> CODEC = RecordCodecBuilder.create(textureGuiElementCodec ->
            textureGuiElementCodec.group(
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                    Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority),
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(TextureGuiElement::getTexture)
            ).apply(textureGuiElementCodec, TextureGuiElement::new)
    );

    public TextureGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority, texture);
    }

    @Override
    public GuiElementType<TextureGuiElement> getType() {
        return Registration.TEXTURE_GUI_ELEMENT.get();
    }
}
