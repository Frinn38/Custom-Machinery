package fr.frinn.custommachinery.common.util.ingredient;

import fr.frinn.custommachinery.common.util.TagUtil;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Predicate;

public class ItemTagIngredient implements IIngredient<Item> {

    private final TagKey<Item> tag;

    private ItemTagIngredient(TagKey<Item> tag) {
        this.tag = tag;
    }

    public static ItemTagIngredient create(String s) throws IllegalArgumentException {
        if(s.startsWith("#"))
            s = s.substring(1);
        if(!Utils.isResourceNameValid(s))
            throw new IllegalArgumentException(String.format("Invalid tag id : %s", s));
        TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(s));
        return new ItemTagIngredient(tag);
    }

    public static ItemTagIngredient create(TagKey<Item> tag) throws IllegalArgumentException {
        return new ItemTagIngredient(tag);
    }

    @Override
    public List<Item> getAll() {
        return TagUtil.getItems(this.tag).toList();
    }

    @Override
    public boolean test(Item item) {
        return TagUtil.getItems(this.tag).anyMatch(Predicate.isEqual(item));
    }

    @Override
    public String toString() {
        return "#" + this.tag.location();
    }
}
