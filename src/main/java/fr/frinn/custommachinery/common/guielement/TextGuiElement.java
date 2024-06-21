package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

import java.util.Locale;

public class TextGuiElement extends AbstractGuiElement {

    public static final NamedCodec<TextGuiElement> CODEC = NamedCodec.record(textGuiElementCodec ->
            textGuiElementCodec.group(
                    makePropertiesCodec().forGetter(TextGuiElement::getProperties),
                    TextComponentUtils.CODEC.fieldOf("text").forGetter(TextGuiElement::getText),
                    NamedCodec.enumCodec(Alignment.class).optionalFieldOf("alignment", Alignment.LEFT).forGetter(TextGuiElement::getAlignment),
                    NamedCodec.BOOL.optionalFieldOf("jei", false).forGetter(IGuiElement::showInJei)
            ).apply(textGuiElementCodec, TextGuiElement::new), "Text gui element"
    );

    private final Component text;
    private final Alignment alignment;
    private final boolean jei;

    public TextGuiElement(Properties properties, Component text, Alignment alignment, boolean jei) {
        super(properties);
        this.text = text;
        this.alignment = alignment;
        this.jei = jei;
    }

    @Override
    public GuiElementType<TextGuiElement> getType() {
        return Registration.TEXT_GUI_ELEMENT.get();
    }

    @Override
    public int getWidth() {
        if(this.getProperties().width() != -1)
            return this.getProperties().width();
        else if(FMLLoader.getDist() == Dist.CLIENT)
            return ClientHandler.textWidth(this.text);
        return -1;
    }

    @Override
    public int getHeight() {
        if(this.getProperties().height() != -1)
            return this.getProperties().height();
        else if(FMLLoader.getDist() == Dist.CLIENT)
            return ClientHandler.getLineHeight();
        return -1;
    }

    public Component getText() {
        return this.text;
    }

    public Alignment getAlignment() {
        return this.alignment;
    }

    @Override
    public boolean showInJei() {
        return this.jei;
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
