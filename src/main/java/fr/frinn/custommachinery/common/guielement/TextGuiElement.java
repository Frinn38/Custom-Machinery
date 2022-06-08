package fr.frinn.custommachinery.common.guielement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.apiimpl.codec.CodecLogger;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Locale;

public class TextGuiElement extends AbstractGuiElement {

    public static final Codec<TextGuiElement> CODEC = RecordCodecBuilder.create(textGuiElementCodec ->
            textGuiElementCodec.group(
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                    Codec.STRING.fieldOf("text").forGetter(element -> element.text.getKey()),
                    CodecLogger.loggedOptional(Codec.INT,"priority", 0).forGetter(AbstractGuiElement::getPriority),
                    CodecLogger.loggedOptional(Codecs.ALIGNMENT_CODEC,"alignment",Alignment.LEFT).forGetter(TextGuiElement::getAlignment),
                    CodecLogger.loggedOptional(Codec.INT,"color", 0).forGetter(TextGuiElement::getColor)
            ).apply(textGuiElementCodec, TextGuiElement::new)
    );

    private final TranslatableComponent text;
    private final Alignment alignment;
    private final int color;

    public TextGuiElement(int x, int y, String text, int priority, Alignment alignment, int color) {
        super(x, y, 0, 0, priority);
        this.text = new TranslatableComponent(text);
        this.alignment = alignment;
        this.color = color;
    }

    @Override
    public GuiElementType<TextGuiElement> getType() {
        return Registration.TEXT_GUI_ELEMENT.get();
    }

    public Component getText() {
        return this.text;
    }

    public Alignment getAlignment() {
        return this.alignment;
    }

    public int getColor() {
        return this.color;
    }

    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT;

        public static Alignment value(String value) {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
    }
}
