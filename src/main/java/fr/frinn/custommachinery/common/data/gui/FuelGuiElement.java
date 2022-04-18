package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractGuiElement;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

public class FuelGuiElement extends AbstractTexturedGuiElement {

    private static final ResourceLocation BASE_EMPTY_TEXURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fuel_empty.png");
    private static final ResourceLocation BASE_FILLED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fuel_filled.png");

    public static final Codec<FuelGuiElement> CODEC = RecordCodecBuilder.create(fuelGuiElement ->
            makeBaseCodec(fuelGuiElement).and(
                fuelGuiElement.group(
                    CodecLogger.loggedOptional(ResourceLocation.CODEC,"emptytexture", BASE_EMPTY_TEXURE).forGetter(FuelGuiElement::getEmptyTexture),
                    CodecLogger.loggedOptional(ResourceLocation.CODEC,"filledtexture", BASE_FILLED_TEXTURE).forGetter(FuelGuiElement::getFilledTexture)
                )
            ).apply(fuelGuiElement, FuelGuiElement::new)
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
