package fr.frinn.custommachinery.common.util.ingredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;

public class ItemIngredient implements IIngredient<Item> {

    private final Item item;

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
        return BuiltInRegistries.ITEM.getKey(this.item).toString();
    }
}
