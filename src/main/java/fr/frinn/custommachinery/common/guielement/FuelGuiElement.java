package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.impl.util.TextureInfo;

public class FuelGuiElement extends AbstractTexturedGuiElement {

    public static final TextureInfo BASE_EMPTY_TEXURE = CustomMachinery.texture("textures/gui/base_fuel_empty.png");
    public static final TextureInfo BASE_FILLED_TEXTURE = CustomMachinery.texture("textures/gui/base_fuel_filled.png");

    public static final NamedCodec<FuelGuiElement> CODEC = NamedCodec.record(fuelGuiElement ->
            fuelGuiElement.group(
                    makePropertiesCodec().forGetter(FuelGuiElement::getProperties),
                    TextureInfo.CODEC.optionalFieldOf("texture_empty", BASE_EMPTY_TEXURE).forGetter(FuelGuiElement::getEmptyTexture),
                    TextureInfo.CODEC.optionalFieldOf("texture_filled", BASE_FILLED_TEXTURE).forGetter(FuelGuiElement::getFilledTexture),
                    NamedCodec.enumCodec(Orientation.class).optionalFieldOf("orientation", Orientation.TOP).aliases("direction").forGetter(FuelGuiElement::getOrientation)
            ).apply(fuelGuiElement, FuelGuiElement::new), "Fuel gui element"
    );

    private final TextureInfo emptyTexture;
    private final TextureInfo filledTexture;
    private final Orientation orientation;

    public FuelGuiElement(Properties properties, TextureInfo emptyTexture, TextureInfo filledTexture, Orientation orientation) {
        super(properties, emptyTexture);
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
        this.orientation = orientation;
    }

    @Override
    public GuiElementType<FuelGuiElement> getType() {
        return Registration.FUEL_GUI_ELEMENT.get();
    }

    public TextureInfo getEmptyTexture() {
        return this.emptyTexture;
    }

    public TextureInfo getFilledTexture() {
        return this.filledTexture;
    }

    public Orientation getOrientation() {
        return this.orientation;
    }
}
