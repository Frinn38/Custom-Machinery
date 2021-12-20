package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractGuiElement;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.common.data.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

public class FluidGuiElement extends AbstractTexturedGuiElement implements IComponentGuiElement<FluidMachineComponent> {

    private static final ResourceLocation BASE_FLUID_STORAGE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fluid_storage.png");

    public static final Codec<FluidGuiElement> CODEC = RecordCodecBuilder.create(fluidGuiElementInstance ->
            fluidGuiElementInstance.group(
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                    Codec.STRING.fieldOf("id").forGetter(FluidGuiElement::getID),
                    CodecLogger.loggedOptional(Codec.intRange(-1, Integer.MAX_VALUE),"width", -1).forGetter(AbstractGuiElement::getWidth),
                    CodecLogger.loggedOptional(Codec.intRange(-1, Integer.MAX_VALUE),"height", -1).forGetter(AbstractGuiElement::getHeight),
                    CodecLogger.loggedOptional(Codec.INT,"priority", 0).forGetter(AbstractGuiElement::getPriority),
                    CodecLogger.loggedOptional(ResourceLocation.CODEC,"texture", BASE_FLUID_STORAGE_TEXTURE).forGetter(FluidGuiElement::getTexture)
            ).apply(fluidGuiElementInstance, FluidGuiElement::new)
    );

    private String id;

    public FluidGuiElement(int x, int y, String id, int width, int height, int priority, ResourceLocation texture) {
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
