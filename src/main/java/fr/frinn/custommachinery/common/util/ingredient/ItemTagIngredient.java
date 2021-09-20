package fr.frinn.custommachinery.common.util.ingredient;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Function;

public class ItemTagIngredient implements IIngredient<Item> {

    private static final Codec<ItemTagIngredient> CODEC_FOR_DATAPACK = Codecs.ITEM_TAG_CODEC.xmap(ItemTagIngredient::new, ingredient -> ingredient.tag);
    private static final Codec<ItemTagIngredient> CODEC_FOR_KUBEJS = Codecs.ITEM_TAG_CODEC.fieldOf("tag").codec().xmap(ItemTagIngredient::new, ingredient -> ingredient.tag);
    public static final Codec<ItemTagIngredient> CODEC = Codecs.either(CODEC_FOR_DATAPACK, CODEC_FOR_KUBEJS, "Item Tag Ingredient")
            .xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);

    private ITag.INamedTag<Item> tag;

    public ItemTagIngredient(ITag.INamedTag<Item> tag) {
        this.tag = tag;
    }

    public ItemTagIngredient(String s) {
        if(s.startsWith("#"))
            s = s.substring(1);
        if(!Utils.isResourceNameValid(s))
            throw new IllegalArgumentException(String.format("Invalid tag id : %s", s));
        this.tag = ItemTags.createOptional(new ResourceLocation(s));
    }

    public ItemTagIngredient(ResourceLocation loc) {
        this.tag = ItemTags.createOptional(loc);
    }

    @Override
    public List<Item> getAll() {
        return this.tag.getAllElements();
    }

    @Override
    public boolean test(Item item) {
        return this.tag.contains(item);
    }

    @Override
    public String toString() {
        return "#" + this.tag.getName();
    }
}
