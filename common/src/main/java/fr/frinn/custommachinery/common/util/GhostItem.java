package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record GhostItem(List<IIngredient<Item>> items, Color color, boolean alwaysRender) {

    private static final Codec<GhostItem> DEFAULT = Codecs.list(IIngredient.ITEM).xmap(items -> new GhostItem(items, Color.TRANSPARENT_WHITE, false), GhostItem::items);

    private static final Codec<GhostItem> COMPLETE = RecordCodecBuilder.create(ghostItemInstance ->
            ghostItemInstance.group(
                    Codecs.list(IIngredient.ITEM).fieldOf("items").forGetter(GhostItem::items),
                    CodecLogger.loggedOptional(Color.CODEC, "color", Color.TRANSPARENT_WHITE).forGetter(GhostItem::color),
                    CodecLogger.loggedOptional(Codec.BOOL, "always_render", false).forGetter(GhostItem::alwaysRender)
            ).apply(ghostItemInstance, GhostItem::new)
    );

    public static final Codec<GhostItem> CODEC = Codecs.either(DEFAULT, COMPLETE, "Ghost Item").xmap(either -> either.map(Function.identity(), Function.identity()), Either::right);

    public static final GhostItem EMPTY = new GhostItem(Collections.emptyList(), Color.TRANSPARENT_WHITE, false);
}
