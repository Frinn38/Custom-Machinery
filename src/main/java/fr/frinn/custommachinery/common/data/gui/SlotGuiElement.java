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
                    Codec.INT.optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.INT.optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(SlotGuiElement::getPriority),
                    Codec.STRING.fieldOf("id").forGetter(SlotGuiElement::getId),
                    ResourceLocation.CODEC.optionalFieldOf("texture", BASE_SLOT_TEXTURE).forGetter(SlotGuiElement::getTexture)
            ).apply(slotGuiElementCodec, SlotGuiElement::new)
    );

    private String id;
    private ResourceLocation texture;

    public SlotGuiElement(int x, int y, int width, int height, int priority, String id, ResourceLocation texture) {
        super(x, y, width, height, priority);
        this.id = id;
        this.texture = texture;
        setBaseTexture(texture);
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
