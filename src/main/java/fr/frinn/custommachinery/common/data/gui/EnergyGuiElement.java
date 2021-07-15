package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

public class EnergyGuiElement extends TexturedGuiElement implements IComponentGuiElement<EnergyMachineComponent> {

    private static final ResourceLocation BASE_ENERGY_STORAGE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_energy_storage.png");

    public static final Codec<EnergyGuiElement> CODEC = RecordCodecBuilder.create(energyGuiElementCodec ->
            energyGuiElementCodec.group(
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                    Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority),
                    ResourceLocation.CODEC.optionalFieldOf("texture", BASE_ENERGY_STORAGE_TEXTURE).forGetter(EnergyGuiElement::getTexture)
            ).apply(energyGuiElementCodec, EnergyGuiElement::new)
    );

    public EnergyGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture) {
        super(x, y, width, height, priority, texture);
    }

    @Override
    public GuiElementType<EnergyGuiElement> getType() {
        return Registration.ENERGY_GUI_ELEMENT.get();
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getComponentType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public String getID() {
        return "";
    }
}
