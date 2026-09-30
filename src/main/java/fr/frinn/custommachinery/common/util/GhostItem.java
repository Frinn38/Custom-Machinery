package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public record GhostItem(List<ItemStack> items, Color color, boolean alwaysRender) {

    private static final NamedCodec<GhostItem> DEFAULT = DefaultCodecs.ITEM_OR_STACK.listOf().xmap(items -> new GhostItem(items, Color.TRANSPARENT_WHITE, false), GhostItem::items, "Ghost item");

    private static final NamedCodec<GhostItem> COMPLETE = NamedCodec.record(ghostItemInstance ->
            ghostItemInstance.group(
                    DefaultCodecs.ITEM_OR_STACK.listOf().fieldOf("items").forGetter(GhostItem::items),
                    Color.CODEC.optionalFieldOf("color", Color.TRANSPARENT_WHITE).forGetter(GhostItem::color),
                    NamedCodec.BOOL.optionalFieldOf("always_render", false).forGetter(GhostItem::alwaysRender)
            ).apply(ghostItemInstance, GhostItem::new), "Ghost item"
    );

    public static final NamedCodec<GhostItem> CODEC = NamedCodec.either(DEFAULT, COMPLETE, "Ghost Item").xmap(either -> either.map(Function.identity(), Function.identity()), Either::right, "Ghost item");

    public static final GhostItem EMPTY = new GhostItem(Collections.emptyList(), Color.TRANSPARENT_WHITE, false);

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(!(obj instanceof GhostItem(List<ItemStack> items1, Color color1, boolean alwaysRender1)))
            return false;
        return items1.equals(this.items) && color1.getARGB() == this.color.getARGB() && alwaysRender1 == this.alwaysRender;
    }
}
