package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.init.Registration;

import java.util.Locale;
import java.util.Optional;

public class TextGuiElement extends AbstractGuiElement {

    public static final Codec<TextGuiElement> CODEC = RecordCodecBuilder.create(textGuiElementCodec ->
            textGuiElementCodec.group(
                    Codec.INT.fieldOf("x").forGetter(TextGuiElement::getX),
                    Codec.INT.fieldOf("y").forGetter(TextGuiElement::getY),
                    Codec.INT.optionalFieldOf("priority").forGetter(element -> Optional.of(element.getPriority())),
                    Codec.STRING.fieldOf("text").forGetter(TextGuiElement::getText),
                    Codec.STRING.optionalFieldOf("alignment").forGetter(text -> Optional.of(text.getAlignment().toString())),
                    Codec.INT.optionalFieldOf("color").forGetter(text -> Optional.of(text.getColor()))
            ).apply(textGuiElementCodec, (x, y, priority, text, alignment, color) -> {
                TextGuiElement element = new TextGuiElement(x, y, priority.orElse(0), text);
                element.alignment = Alignment.value(alignment.orElse("LEFT"));
                element.color = color.orElse(0);
                return element;
            })
    );

    private String text;
    private Alignment alignment;
    private int color;

    public TextGuiElement(int x, int y, int priority, String text) {
        super(x, y, 0, 0, priority);
        this.text = text;
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
    }
}
