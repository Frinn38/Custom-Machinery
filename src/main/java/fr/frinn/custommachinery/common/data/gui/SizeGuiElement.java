package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractGuiElement;
import fr.frinn.custommachinery.common.init.Registration;

public class SizeGuiElement extends AbstractGuiElement {

    public static final Codec<SizeGuiElement> CODEC = RecordCodecBuilder.create(sizeGuiElement ->
            sizeGuiElement.group(
                    Codec.intRange(1, 3840).optionalFieldOf("width", 256).forGetter(SizeGuiElement::getWidth),
                    Codec.intRange(1, 2160).optionalFieldOf("height", 192).forGetter(SizeGuiElement::getHeight)
            ).apply(sizeGuiElement, SizeGuiElement::new)
    );

    private final int width;
    private final int height;

    public SizeGuiElement(int width, int height) {
        super(0, 0, 0, 0, 0);
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public GuiElementType<SizeGuiElement> getType() {
        return Registration.SIZE_GUI_ELEMENT.get();
    }
}
