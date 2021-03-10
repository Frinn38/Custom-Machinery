package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class FluidGuiElement extends AbstractGuiElement {

    private static final ResourceLocation BASE_FLUID_STORAGE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_fluid_storage.png");

    public static final Codec<FluidGuiElement> CODEC = RecordCodecBuilder.create(fluidGuiElementInstance ->
            fluidGuiElementInstance.group(
                    Codec.INT.fieldOf("x").forGetter(FluidGuiElement::getX),
                    Codec.INT.fieldOf("y").forGetter(FluidGuiElement::getY),
                    Codec.INT.optionalFieldOf("width").forGetter(element -> Optional.of(element.getWidth())),
                    Codec.INT.optionalFieldOf("height").forGetter(element -> Optional.of(element.getHeight())),
                    Codec.INT.optionalFieldOf("priority").forGetter(element -> Optional.of(element.getPriority())),
                    Codec.STRING.fieldOf("id").forGetter(FluidGuiElement::getId),
                    ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(element -> Optional.of(element.getTexture()))
            ).apply(fluidGuiElementInstance, (x, y, width, height, priority, id, texture) ->
                    new FluidGuiElement(x, y, width.orElse(-1), height.orElse(-1), priority.orElse(0), id, texture.orElse(BASE_FLUID_STORAGE_TEXTURE)))
    );

    private String id;
    private ResourceLocation texture;

    public FluidGuiElement(int x, int y, int width, int height, int priority, String id, ResourceLocation texture) {
        super(x, y, width, height, priority);
        this.id = id;
        this.texture = texture;
        setBaseTexture(texture);
    }

    @Override
    public GuiElementType getType() {
        return Registration.FLUID_GUI_ELEMENT.get();
    }

    public String getId() {
        return this.id;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }
}
