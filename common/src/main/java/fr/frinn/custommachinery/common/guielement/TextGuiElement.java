package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Locale;

public class TextGuiElement extends AbstractGuiElement {

    public static final NamedCodec<TextGuiElement> CODEC = NamedCodec.record(textGuiElementCodec ->
            textGuiElementCodec.group(
                    NamedCodec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                    NamedCodec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                    NamedCodec.STRING.fieldOf("text").forGetter(element -> element.text.getKey()),
                    NamedCodec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority),
                    Codecs.ALIGNMENT_CODEC.optionalFieldOf("alignment", Alignment.LEFT).forGetter(TextGuiElement::getAlignment),
                    NamedCodec.INT.optionalFieldOf("color", 0).forGetter(TextGuiElement::getColor)
            ).apply(textGuiElementCodec, TextGuiElement::new), "Text gui element"
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
