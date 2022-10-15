package fr.frinn.custommachinery.common.guielement;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Color;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;

public class SlotGuiElement extends AbstractTexturedGuiElement implements IComponentGuiElement<ItemMachineComponent> {

    private static final ResourceLocation BASE_SLOT_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_slot.png");

    public static final Codec<SlotGuiElement> CODEC = RecordCodecBuilder.create(slotGuiElementCodec ->
            make(slotGuiElementCodec, slotGuiElementCodec.group(
                    Codec.STRING.fieldOf("id").forGetter(SlotGuiElement::getID),
                    CodecLogger.loggedOptional(Codecs.list(IIngredient.ITEM),"item", Collections.emptyList()).forGetter(SlotGuiElement::getItems),
                    CodecLogger.loggedOptional(Color.CODEC, "color", Color.WHITE).forGetter(SlotGuiElement::getColor)
            )).apply(slotGuiElementCodec, SlotGuiElement::new)
    );

    private final String id;
    private final List<IIngredient<Item>> item;
    private final Color color;

    public SlotGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture, String id, List<IIngredient<Item>> item, Color color) {
        super(x, y, width, height, priority, texture);
        this.id = id;
        this.item = item;
        this.color = color;
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

    public Color getColor() {
        return this.color;
    }

    private static <T7, T8, T9> Products.P9<Mu<SlotGuiElement>, Integer, Integer, Integer, Integer, Integer, ResourceLocation, T7, T8, T9> make(RecordCodecBuilder.Instance<SlotGuiElement> instance, Products.P3<Mu<SlotGuiElement>, T7, T8, T9> parts) {
        var base = makeBaseTexturedCodec(instance, BASE_SLOT_TEXTURE);
        return new Products.P9<>(base.t1(), base.t2(), base.t3(), base.t4(), base.t5(), base.t6(), parts.t1(), parts.t2(), parts.t3());
    }
}
