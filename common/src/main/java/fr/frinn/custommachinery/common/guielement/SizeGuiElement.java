package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement;

import java.util.Collections;

public class SizeGuiElement extends AbstractGuiElement {

    public static final NamedCodec<SizeGuiElement> CODEC = NamedCodec.record(sizeGuiElement ->
            sizeGuiElement.group(
                    NamedCodec.intRange(1, 3840).optionalFieldOf("width", 256).forGetter(SizeGuiElement::getWidth),
                    NamedCodec.intRange(1, 2160).optionalFieldOf("height", 192).forGetter(SizeGuiElement::getHeight)
            ).apply(sizeGuiElement, SizeGuiElement::new), "Size gui element"
    );

    private final int width;
    private final int height;

    public SizeGuiElement(int width, int height) {
        super(new Properties(0, 0, 0, 0, 0, null, null, Collections.emptyList(), ""));
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
