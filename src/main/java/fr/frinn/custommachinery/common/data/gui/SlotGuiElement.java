package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class SlotGuiElement extends AbstractGuiElement {

    private static final ResourceLocation BASE_SLOT_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_slot.png");

    public static final Codec<SlotGuiElement> CODEC = RecordCodecBuilder.create(slotGuiElementCodec ->
            slotGuiElementCodec.group(
                    Codec.INT.fieldOf("x").forGetter(SlotGuiElement::getX),
                    Codec.INT.fieldOf("y").forGetter(SlotGuiElement::getY),
                    Codec.INT.optionalFieldOf("width").forGetter(element -> Optional.of(element.getWidth())),
                    Codec.INT.optionalFieldOf("height").forGetter(element -> Optional.of(element.getHeight())),
                    Codec.INT.optionalFieldOf("priority").forGetter(element -> Optional.of(element.getPriority())),
                    Codec.STRING.fieldOf("id").forGetter(SlotGuiElement::getId),
                    ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(element -> Optional.of(element.getTexture()))
            ).apply(slotGuiElementCodec, (x, y, width, height, priority, id, texture) ->
                    new SlotGuiElement(x, y, width.orElse(-1), height.orElse(-1), priority.orElse(0), id, texture.orElse(BASE_SLOT_TEXTURE))
            )
    );

    private String id;
    private ResourceLocation texture;

    public SlotGuiElement(int x, int y, int width, int height, int priority, String id, ResourceLocation texture) {
        super(x, y, width, height, priority);
        this.id = id;
        this.texture = texture;
    }

    @Override
    public GuiElementType getType() {
        return Registration.SLOT_GUI_ELEMENT.get();
    }

    public String getId() {
        return this.id;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }
}
