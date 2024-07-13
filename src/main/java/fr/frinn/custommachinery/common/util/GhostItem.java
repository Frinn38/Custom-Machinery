package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CraftingHelper;

import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public record GhostItem(Ingredient ingredient, Color color, boolean alwaysRender) {

    private static final NamedCodec<GhostItem> DEFAULT = NamedCodec.of(CraftingHelper.makeIngredientCodec(true)).xmap(items -> new GhostItem(items, Color.TRANSPARENT_WHITE, false), GhostItem::ingredient, "Ghost item");

    private static final NamedCodec<GhostItem> COMPLETE = NamedCodec.record(ghostItemInstance ->
            ghostItemInstance.group(
                    NamedCodec.of(CraftingHelper.makeIngredientCodec(true)).fieldOf("items").forGetter(GhostItem::ingredient),
                    Color.CODEC.optionalFieldOf("color", Color.TRANSPARENT_WHITE).forGetter(GhostItem::color),
                    NamedCodec.BOOL.optionalFieldOf("always_render", false).forGetter(GhostItem::alwaysRender)
            ).apply(ghostItemInstance, GhostItem::new), "Ghost item"
    );

    public static final NamedCodec<GhostItem> CODEC = NamedCodec.either(DEFAULT, COMPLETE, "Ghost Item").xmap(either -> either.map(Function.identity(), Function.identity()), Either::right, "Ghost item");

    public static final GhostItem EMPTY = new GhostItem(Ingredient.EMPTY, Color.TRANSPARENT_WHITE, false);

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(!(obj instanceof GhostItem ghost))
            return false;
        return ghost.ingredient.equals(this.ingredient) && ghost.color.getARGB() == this.color.getARGB() && ghost.alwaysRender == this.alwaysRender;
    }
}
