package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class TextureGuiElement extends AbstractGuiElement {

    public static final Codec<TextureGuiElement> CODEC = RecordCodecBuilder.create(textureGuiElementCodec ->
            textureGuiElementCodec.group(
                    Codec.INT.fieldOf("x").forGetter(TextureGuiElement::getX),
                    Codec.INT.fieldOf("y").forGetter(TextureGuiElement::getY),
                    Codec.INT.optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.INT.optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority),
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(TextureGuiElement::getTexture)
            ).apply(textureGuiElementCodec, TextureGuiElement::new)
    );

    private ResourceLocation texture;

    public TextureGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority);
        this.texture = texture;
        setBaseTexture(texture);
    }

    @Override
    public GuiElementType getType() {
        return Registration.TEXTURE_GUI_ELEMENT.get();
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }
}
