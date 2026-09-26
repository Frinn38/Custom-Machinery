package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Function;

public record GhostItem(Ingredient ingredient, Color color, boolean alwaysRender) {

    private static final NamedCodec<GhostItem> DEFAULT = NamedCodec.of(Ingredient.CODEC).xmap(items -> new GhostItem(items, Color.TRANSPARENT_WHITE, false), GhostItem::ingredient, "Ghost item");

    private static final NamedCodec<GhostItem> COMPLETE = NamedCodec.record(ghostItemInstance ->
            ghostItemInstance.group(
                    NamedCodec.of(Ingredient.CODEC).fieldOf("items").forGetter(GhostItem::ingredient),
                    Color.CODEC.optionalFieldOf("color", Color.TRANSPARENT_WHITE).forGetter(GhostItem::color),
                    NamedCodec.BOOL.optionalFieldOf("always_render", false).forGetter(GhostItem::alwaysRender)
            ).apply(ghostItemInstance, GhostItem::new), "Ghost item"
    );

    public static final NamedCodec<GhostItem> CODEC = NamedCodec.either(DEFAULT, COMPLETE, "Ghost Item").xmap(either -> either.map(Function.identity(), Function.identity()), Either::right, "Ghost item");

    public static final GhostItem EMPTY = new GhostItem(Ingredient.of(Items.AIR), Color.TRANSPARENT_WHITE, false);

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(!(obj instanceof GhostItem(Ingredient ingredient1, Color color1, boolean render)))
            return false;
        return ingredient1.equals(this.ingredient) && color1.getARGB() == this.color.getARGB() && render == this.alwaysRender;
    }
}
