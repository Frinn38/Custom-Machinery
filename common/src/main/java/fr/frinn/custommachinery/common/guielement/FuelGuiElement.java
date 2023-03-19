package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

public class FuelGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_EMPTY_TEXURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fuel_empty.png");
    private static final ResourceLocation BASE_FILLED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fuel_filled.png");

    public static final NamedCodec<FuelGuiElement> CODEC = NamedCodec.record(fuelGuiElement ->
            makeBaseCodec(fuelGuiElement).and(
                    fuelGuiElement.group(
                            DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("emptytexture", BASE_EMPTY_TEXURE).forGetter(FuelGuiElement::getEmptyTexture),
                            DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("filledtexture", BASE_FILLED_TEXTURE).forGetter(FuelGuiElement::getFilledTexture)
                    )
            ).apply(fuelGuiElement, FuelGuiElement::new), "Fuel gui element"
    );

    private final ResourceLocation emptyTexture;
    private final ResourceLocation filledTexture;

    public FuelGuiElement(int x, int y, int width, int height, int priority, ResourceLocation emptyTexture, ResourceLocation filledTexture) {
        super(x, y, width, height, priority, emptyTexture);
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
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
}
