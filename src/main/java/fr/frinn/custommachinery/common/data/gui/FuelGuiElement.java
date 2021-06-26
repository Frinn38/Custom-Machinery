package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

public class FuelGuiElement extends TexturedGuiElement {

    private static final ResourceLocation BASE_EMPTY_TEXURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fuel_empty.png");
    private static final ResourceLocation BASE_FILLED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fuel_filled.png");

    public static final Codec<FuelGuiElement> CODEC = RecordCodecBuilder.create(fuelGuiElement ->
            fuelGuiElement.group(
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                    Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority),
                    ResourceLocation.CODEC.optionalFieldOf("emptytexture", BASE_EMPTY_TEXURE).forGetter(FuelGuiElement::getEmptyTexture),
                    ResourceLocation.CODEC.optionalFieldOf("filledtexture", BASE_FILLED_TEXTURE).forGetter(FuelGuiElement::getFilledTexture)
            ).apply(fuelGuiElement, FuelGuiElement::new)
    );

    private ResourceLocation emptyTexture;
    private ResourceLocation filledTexture;

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
