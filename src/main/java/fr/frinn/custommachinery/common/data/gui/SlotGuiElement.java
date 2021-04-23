package fr.frinn.custommachinery.common.data.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class SlotGuiElement extends AbstractGuiElement {

    private static final ResourceLocation BASE_SLOT_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_slot.png");

    @SuppressWarnings("deprecation")
    public static final Codec<SlotGuiElement> CODEC = RecordCodecBuilder.create(slotGuiElementCodec ->
            slotGuiElementCodec.group(
                    Codec.INT.fieldOf("x").forGetter(SlotGuiElement::getX),
                    Codec.INT.fieldOf("y").forGetter(SlotGuiElement::getY),
                    Codec.INT.optionalFieldOf("width", -1).forGetter(AbstractGuiElement::getWidth),
                    Codec.INT.optionalFieldOf("height", -1).forGetter(AbstractGuiElement::getHeight),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(SlotGuiElement::getPriority),
                    Codec.STRING.fieldOf("id").forGetter(SlotGuiElement::getId),
                    ResourceLocation.CODEC.optionalFieldOf("texture", BASE_SLOT_TEXTURE).forGetter(SlotGuiElement::getTexture),
                    Registry.ITEM.optionalFieldOf("item", Items.AIR).forGetter(SlotGuiElement::getItem)
            ).apply(slotGuiElementCodec, SlotGuiElement::new)
    );

    private String id;
    private ResourceLocation texture;
    private Item item;

    public SlotGuiElement(int x, int y, int width, int height, int priority, String id, ResourceLocation texture, Item item) {
        super(x, y, width, height, priority);
        this.id = id;
        this.texture = texture;
        this.item = item;
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

    public Item getItem() {
        return this.item;
    }
}
