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
import fr.frinn.custommachinery.common.data.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

public class EnergyGuiElement extends AbstractTexturedGuiElement implements IComponentGuiElement<EnergyMachineComponent> {

    private static final ResourceLocation BASE_ENERGY_STORAGE_EMPTY_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_energy_storage_empty.png");
    private static final ResourceLocation BASE_ENERGY_STORAGE_FILLED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_energy_storage_filled.png");

    public static final Codec<EnergyGuiElement> CODEC = RecordCodecBuilder.create(energyGuiElementCodec ->
            energyGuiElementCodec.group(
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("x").forGetter(AbstractGuiElement::getX),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("y").forGetter(AbstractGuiElement::getY),
                    CodecLogger.loggedOptional(Codec.intRange(-1, Integer.MAX_VALUE),"width", -1).forGetter(AbstractGuiElement::getWidth),
                    CodecLogger.loggedOptional(Codec.intRange(-1, Integer.MAX_VALUE),"height", -1).forGetter(AbstractGuiElement::getHeight),
                    CodecLogger.loggedOptional(Codec.INT,"priority", 0).forGetter(AbstractGuiElement::getPriority),
                    CodecLogger.loggedOptional(ResourceLocation.CODEC,"emptytexture", BASE_ENERGY_STORAGE_EMPTY_TEXTURE).forGetter(EnergyGuiElement::getEmptyTexture),
                    CodecLogger.loggedOptional(ResourceLocation.CODEC,"filledtexture", BASE_ENERGY_STORAGE_FILLED_TEXTURE).forGetter(EnergyGuiElement::getFilledTexture)
            ).apply(energyGuiElementCodec, EnergyGuiElement::new)
    );

    private final ResourceLocation emptyTexture;
    private final ResourceLocation filledTexture;

    public EnergyGuiElement(int x, int y, int width, int height, int priority, ResourceLocation emptyTexture, ResourceLocation filledTexture) {
        super(x, y, width, height, priority, emptyTexture);
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
    }

    public ResourceLocation getEmptyTexture() {
        return this.emptyTexture;
    }

    public ResourceLocation getFilledTexture() {
        return this.filledTexture;
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
