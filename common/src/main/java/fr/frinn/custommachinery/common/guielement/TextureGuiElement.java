package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;

public class TextureGuiElement extends AbstractTexturedGuiElement {

    public static final NamedCodec<TextureGuiElement> CODEC = NamedCodec.record(textureGuiElement ->
            textureGuiElement.group(
                    makePropertiesCodec().forGetter(TextureGuiElement::getProperties),
                    NamedCodec.BOOL.optionalFieldOf("jei", false).forGetter(IGuiElement::showInJei)
            ).apply(textureGuiElement, TextureGuiElement::new), "Texture gui element"
    );

    private final boolean jei;

    public TextureGuiElement(Properties properties, boolean jei) {
        super(properties);
        this.jei = jei;
    }

    @Override
    public GuiElementType<TextureGuiElement> getType() {
        return Registration.TEXTURE_GUI_ELEMENT.get();
    }

    @Override
    public boolean showInJei() {
        return this.jei;
    }
}
