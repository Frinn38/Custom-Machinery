package fr.frinn.custommachinery.common.guielement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.apiimpl.codec.CodecLogger;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;

public class SlotGuiElement extends AbstractTexturedGuiElement implements IComponentGuiElement<ItemMachineComponent> {

    private static final ResourceLocation BASE_SLOT_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_slot.png");

    public static final Codec<SlotGuiElement> CODEC = RecordCodecBuilder.create(slotGuiElementCodec ->
            makeBaseTexturedCodec(slotGuiElementCodec, BASE_SLOT_TEXTURE).and(
                    slotGuiElementCodec.group(
                            Codec.STRING.fieldOf("id").forGetter(SlotGuiElement::getID),
                            CodecLogger.loggedOptional(Codecs.list(IIngredient.ITEM),"item", Collections.emptyList()).forGetter(SlotGuiElement::getItems)
                    )
            ).apply(slotGuiElementCodec, SlotGuiElement::new)
    );

    private final String id;
    private final List<IIngredient<Item>> item;

    public SlotGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture, String id, List<IIngredient<Item>> item) {
        super(x, y, width, height, priority, texture);
        this.id = id;
        this.item = item;
    }

    @Override
    public GuiElementType<SlotGuiElement> getType() {
        return Registration.SLOT_GUI_ELEMENT.get();
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    public List<IIngredient<Item>> getItems() {
        return this.item;
    }
}
