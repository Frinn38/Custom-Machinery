package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractGuiElement;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

public class TextureGuiElement extends AbstractTexturedGuiElement {

    public static final Codec<TextureGuiElement> CODEC = RecordCodecBuilder.create(textureGuiElementCodec ->
            textureGuiElementCodec.group(
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(TextureGuiElement::getTexture),
                    CodecLogger.loggedOptional(Codec.intRange(-1, Integer.MAX_VALUE),"width", -1).forGetter(AbstractGuiElement::getWidth),
                    CodecLogger.loggedOptional(Codec.intRange(-1, Integer.MAX_VALUE),"height", -1).forGetter(AbstractGuiElement::getHeight),
                    CodecLogger.loggedOptional(Codec.INT,"priority", 0).forGetter(AbstractGuiElement::getPriority)
            ).apply(textureGuiElementCodec, TextureGuiElement::new)
    );

    public TextureGuiElement(int x, int y, ResourceLocation texture, int width, int height, int priority) {
        super(x, y, width, height, priority, texture);
    }

    @Override
    public GuiElementType<TextureGuiElement> getType() {
        return Registration.TEXTURE_GUI_ELEMENT.get();
    }
}
