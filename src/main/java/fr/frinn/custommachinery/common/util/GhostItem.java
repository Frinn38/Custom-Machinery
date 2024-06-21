package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record GhostItem(List<IIngredient<Item>> items, Color color, boolean alwaysRender) {

    private static final NamedCodec<GhostItem> DEFAULT = IIngredient.ITEM.listOf().xmap(items -> new GhostItem(items, Color.TRANSPARENT_WHITE, false), GhostItem::items, "Ghost item");

    private static final NamedCodec<GhostItem> COMPLETE = NamedCodec.record(ghostItemInstance ->
            ghostItemInstance.group(
                    IIngredient.ITEM.listOf().fieldOf("items").forGetter(GhostItem::items),
                    Color.CODEC.optionalFieldOf("color", Color.TRANSPARENT_WHITE).forGetter(GhostItem::color),
                    NamedCodec.BOOL.optionalFieldOf("always_render", false).forGetter(GhostItem::alwaysRender)
            ).apply(ghostItemInstance, GhostItem::new), "Ghost item"
    );

    public static final NamedCodec<GhostItem> CODEC = NamedCodec.either(DEFAULT, COMPLETE, "Ghost Item").xmap(either -> either.map(Function.identity(), Function.identity()), Either::right, "Ghost item");

    public static final GhostItem EMPTY = new GhostItem(Collections.emptyList(), Color.TRANSPARENT_WHITE, false);
}
