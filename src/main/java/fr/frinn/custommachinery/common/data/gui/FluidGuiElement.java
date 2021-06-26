package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

public class FluidGuiElement extends TexturedGuiElement implements IComponentGuiElement<FluidMachineComponent> {

    private static final ResourceLocation BASE_FLUID_STORAGE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fluid_storage.png");

    public static final Codec<FluidGuiElement> CODEC = RecordCodecBuilder.create(fluidGuiElementInstance ->
            fluidGuiElementInstance.group(
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                    Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(AbstractGuiElement::getPriority),
                    Codec.STRING.fieldOf("id").forGetter(FluidGuiElement::getID),
                    ResourceLocation.CODEC.optionalFieldOf("texture", BASE_FLUID_STORAGE_TEXTURE).forGetter(FluidGuiElement::getTexture)
            ).apply(fluidGuiElementInstance, FluidGuiElement::new)
    );

    private String id;

    public FluidGuiElement(int x, int y, int width, int height, int priority, String id, ResourceLocation texture) {
        super(x, y, width, height, priority, texture);
        this.id = id;
    }

    @Override
    public GuiElementType<FluidGuiElement> getType() {
        return Registration.FLUID_GUI_ELEMENT.get();
    }

    @Override
    public MachineComponentType<FluidMachineComponent> getComponentType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public String getID() {
        return this.id;
    }
}
