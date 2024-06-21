package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement;

import java.util.Collections;

public class EmptyGuiElement extends AbstractGuiElement {

    public static final NamedCodec<EmptyGuiElement> CODEC = NamedCodec.record(emptyGuiElementInstance ->
            emptyGuiElementInstance.group(
                    NamedCodec.STRING.optionalFieldOf("id", "").forGetter(AbstractGuiElement::getId)
            ).apply(emptyGuiElementInstance, EmptyGuiElement::new), "Empty gui element"
    );

    public EmptyGuiElement(String id) {
        super(new Properties(0, 0, 0, 0, 0, null, null, Collections.emptyList(), id));
    }

    @Override
    public GuiElementType<EmptyGuiElement> getType() {
        return Registration.EMPTY_GUI_ELEMENT.get();
    }
}
