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
                    Codec.INT.optionalFieldOf("width").forGetter(element -> Optional.of(element.getWidth())),
                    Codec.INT.optionalFieldOf("height").forGetter(element -> Optional.of(element.getHeight())),
                    Codec.INT.optionalFieldOf("priority").forGetter(element -> Optional.of(element.getPriority())),
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(TextureGuiElement::getTexture)
            ).apply(textureGuiElementCodec, (x, y, width, height, priority, texture) ->
                    new TextureGuiElement(x, y, width.orElse(-1), height.orElse(-1), priority.orElse(0), texture)
            )
    );

    private ResourceLocation texture;

    public TextureGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority);
        this.texture = texture;
    }

    @Override
    public GuiElementType getType() {
        return Registration.TEXTURE_GUI_ELEMENT.get();
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }
}
