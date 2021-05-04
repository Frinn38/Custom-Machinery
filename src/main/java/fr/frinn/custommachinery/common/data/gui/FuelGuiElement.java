package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

public class FuelGuiElement extends AbstractGuiElement {

    private static final ResourceLocation BASE_EMPTY_TEXURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fuel_empty.png");
    private static final ResourceLocation BASE_FILLED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fuel_filled.png");

    public static final Codec<FuelGuiElement> CODEC = RecordCodecBuilder.create(fuelGuiElement ->
            fuelGuiElement.group(
                    Codec.INT.fieldOf("x").forGetter(AbstractGuiElement::getX),
                    Codec.INT.fieldOf("y").forGetter(AbstractGuiElement::getY),
                    Codec.INT.optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.INT.optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", -1).forGetter(AbstractGuiElement::getPriority),
                    ResourceLocation.CODEC.optionalFieldOf("emptytexture", BASE_EMPTY_TEXURE).forGetter(FuelGuiElement::getEmptyTexture),
                    ResourceLocation.CODEC.optionalFieldOf("filledtexture", BASE_FILLED_TEXTURE).forGetter(FuelGuiElement::getFilledTexture)
            ).apply(fuelGuiElement, FuelGuiElement::new)
    );

    private ResourceLocation emptyTexture;
    private ResourceLocation filledTexture;

    public FuelGuiElement(int x, int y, int width, int height, int priority, ResourceLocation emptyTexture, ResourceLocation filledTexture) {
        super(x, y, width, height, priority);
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
        this.setBaseTexture(this.emptyTexture);
    }

    @Override
    public GuiElementType getType() {
        return Registration.FUEL_GUI_ELEMENT.get();
    }

    public ResourceLocation getEmptyTexture() {
        return this.emptyTexture;
    }

    public ResourceLocation getFilledTexture() {
        return this.filledTexture;
    }
}
