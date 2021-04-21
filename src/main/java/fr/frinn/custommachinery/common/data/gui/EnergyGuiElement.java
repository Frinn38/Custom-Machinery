package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class EnergyGuiElement extends AbstractGuiElement {

    private static final ResourceLocation BASE_ENERGY_STORAGE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_energy_storage.png");

    public static final Codec<EnergyGuiElement> CODEC = RecordCodecBuilder.create(energyGuiElementCodec ->
            energyGuiElementCodec.group(
                    Codec.INT.fieldOf("x").forGetter(EnergyGuiElement::getX),
                    Codec.INT.fieldOf("y").forGetter(EnergyGuiElement::getY),
                    Codec.INT.optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.INT.optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", -1).forGetter(AbstractGuiElement::getPriority),
                    ResourceLocation.CODEC.optionalFieldOf("texture", BASE_ENERGY_STORAGE_TEXTURE).forGetter(EnergyGuiElement::getTexture)
            ).apply(energyGuiElementCodec, EnergyGuiElement::new)
    );

    private ResourceLocation texture;

    public EnergyGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority);
        this.texture = texture;
        setBaseTexture(texture);
    }

    @Override
    public GuiElementType getType() {
        return Registration.ENERGY_GUI_ELEMENT.get();
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }
}
