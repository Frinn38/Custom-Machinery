package fr.frinn.custommachinery.common.data.gui;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.apiimpl.guielement.AbstractTexturedGuiElement;
import fr.frinn.custommachinery.common.data.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class SlotGuiElement extends AbstractTexturedGuiElement implements IComponentGuiElement<ItemMachineComponent> {

    private static final ResourceLocation BASE_SLOT_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_slot.png");

    public static final Codec<SlotGuiElement> CODEC = RecordCodecBuilder.create(slotGuiElementCodec ->
            make(
                    makeBaseTexturedCodec(slotGuiElementCodec, BASE_SLOT_TEXTURE),
                    slotGuiElementCodec.group(
                            Codec.STRING.fieldOf("id").forGetter(SlotGuiElement::getID),
                            CodecLogger.loggedOptional(Codecs.list(IIngredient.ITEM),"item", Collections.emptyList()).forGetter(SlotGuiElement::getItems),
                            CodecLogger.loggedOptional(Codec.BOOL, "always_render", false).forGetter(SlotGuiElement::alwaysRender)
                    )
            ).apply(slotGuiElementCodec, SlotGuiElement::new)
    );

    private final String id;
    private final List<IIngredient<Item>> item;
    private final boolean alwaysRender;

    public SlotGuiElement(int x, int y, int width, int height, int priority, ResourceLocation texture, String id, List<IIngredient<Item>> item, boolean alwaysRender) {
        super(x, y, width, height, priority, texture);
        this.id = id;
        this.item = item;
        this.alwaysRender = alwaysRender;
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

    public boolean alwaysRender() {
        return this.alwaysRender;
    }

    private static <F extends RecordCodecBuilder.Mu<?>, T1, T2, T3, T4, T5, T6, T7, T8, T9> Products.P9<F, T1, T2, T3, T4, T5, T6, T7, T8, T9> make(Products.P6<F, T1, T2, T3, T4, T5, T6> base, Products.P3<F, T7, T8, T9> other) {
        return new Products.P9<>(base.t1(), base.t2(), base.t3(), base.t4(), base.t5(), base.t6(), other.t1(), other.t2(), other.t3());
    }
}
