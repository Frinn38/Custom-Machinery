package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

public class FuelGuiElement extends AbstractTexturedGuiElement {

    public static final ResourceLocation BASE_EMPTY_TEXURE = CustomMachinery.rl("textures/gui/base_fuel_empty.png");
    public static final ResourceLocation BASE_FILLED_TEXTURE = CustomMachinery.rl("textures/gui/base_fuel_filled.png");

    public static final NamedCodec<FuelGuiElement> CODEC = NamedCodec.record(fuelGuiElement ->
            fuelGuiElement.group(
                    makePropertiesCodec().forGetter(FuelGuiElement::getProperties),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_empty", BASE_EMPTY_TEXURE).forGetter(FuelGuiElement::getEmptyTexture),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("texture_filled", BASE_FILLED_TEXTURE).forGetter(FuelGuiElement::getFilledTexture),
                    NamedCodec.enumCodec(Orientation.class).optionalFieldOf("orientation", Orientation.TOP).aliases("direction").forGetter(FuelGuiElement::getOrientation)
            ).apply(fuelGuiElement, FuelGuiElement::new), "Fuel gui element"
    );

    private final ResourceLocation emptyTexture;
    private final ResourceLocation filledTexture;
    private final Orientation orientation;

    public FuelGuiElement(Properties properties, ResourceLocation emptyTexture, ResourceLocation filledTexture, Orientation orientation) {
        super(properties, emptyTexture);
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
        this.orientation = orientation;
    }

    @Override
    public GuiElementType<FuelGuiElement> getType() {
        return Registration.FUEL_GUI_ELEMENT.get();
    }

    public ResourceLocation getEmptyTexture() {
        return this.emptyTexture;
    }

    public ResourceLocation getFilledTexture() {
        return this.filledTexture;
    }

    public Orientation getOrientation() {
        return this.orientation;
    }
}
