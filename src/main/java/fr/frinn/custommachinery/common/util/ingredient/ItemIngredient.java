package fr.frinn.custommachinery.common.util.ingredient;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.codec.RegistryCodec;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.item.Item;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ItemIngredient implements IIngredient<Item> {

    private static final Codec<ItemIngredient> CODEC_FOR_DATAPACK = RegistryCodec.ITEM.xmap(ItemIngredient::new, ingredient -> ingredient.item);
    private static final Codec<ItemIngredient> CODEC_FOR_KUBEJS = RegistryCodec.ITEM.fieldOf("item").codec().xmap(ItemIngredient::new, ingredient -> ingredient.item);
    public static final Codec<ItemIngredient> CODEC = Codecs.either(CODEC_FOR_DATAPACK, CODEC_FOR_KUBEJS, "Item Ingredient")
            .xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);

    private Item item;

    public ItemIngredient(Item item) {
        this.item = item;
    }

    @Override
    public List<Item> getAll() {
        return Collections.singletonList(this.item);
    }

    @Override
    public boolean test(Item item) {
        return this.item == item;
    }

    @Override
    public String toString() {
        return this.item.getRegistryName().toString();
    }
}
