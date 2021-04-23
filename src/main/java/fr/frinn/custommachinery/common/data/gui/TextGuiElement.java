package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;

import java.util.Locale;

public class TextGuiElement extends AbstractGuiElement {

    public static final Codec<TextGuiElement> CODEC = RecordCodecBuilder.create(textGuiElementCodec ->
            textGuiElementCodec.group(
                    Codec.INT.fieldOf("x").forGetter(AbstractGuiElement::getX),
                    Codec.INT.fieldOf("y").forGetter(AbstractGuiElement::getY),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority),
                    Codec.STRING.fieldOf("text").forGetter(TextGuiElement::getText),
                    Codecs.ALIGNMENT_CODEC.optionalFieldOf("alignment",Alignment.LEFT).forGetter(TextGuiElement::getAlignment),
                    Codec.INT.optionalFieldOf("color", 0).forGetter(TextGuiElement::getColor)
            ).apply(textGuiElementCodec, TextGuiElement::new)
    );

    private String text;
    private Alignment alignment;
    private int color;

    public TextGuiElement(int x, int y, int priority, String text, Alignment alignment, int color) {
        super(x, y, 0, 0, priority);
        this.text = text;
        this.alignment = alignment;
        this.color = color;
    }

    @Override
    public GuiElementType getType() {
        return Registration.TEXT_GUI_ELEMENT.get();
    }

    public String getText() {
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
