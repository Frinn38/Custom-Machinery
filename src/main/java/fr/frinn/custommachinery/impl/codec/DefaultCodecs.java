package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.machine.CustomMachineJsonReloadListener;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public class DefaultCodecs {

    public static final NamedCodec<ResourceLocation> RESOURCE_LOCATION = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeResourceLocation, ResourceLocation::toString, "Resource location");
    public static final NamedCodec<Character> CHARACTER = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeCharacter, Object::toString, "Character");

    public static final NamedCodec<SoundEvent> SOUND_EVENT = RESOURCE_LOCATION.xmap(SoundEvent::createVariableRangeEvent, SoundEvent::getLocation, "Sound event");

    public static final NamedCodec<Direction> DIRECTION = NamedCodec.enumCodec(Direction.class);

    public static final NamedCodec<ItemStack> ITEM_OR_STACK = NamedCodec.either(RegistrarCodec.ITEM, NamedCodec.of(ItemStack.OPTIONAL_CODEC), "ItemStack").xmap(either -> either.map(Item::getDefaultInstance, Function.identity()), Either::right, "Item Stack");

    public static final NamedCodec<Ingredient> INGREDIENT = NamedCodec.of(Ingredient.CODEC, "Ingredient");

    public static final NamedCodec<SizedIngredient> SIZED_INGREDIENT_WITH_NBT = NamedCodec.record(sizedIngredientInstance ->
            sizedIngredientInstance.group(
                    ITEM_OR_STACK.listOf().fieldOf("item").forGetter(ingredient -> Arrays.asList(ingredient.getItems())),
                    NamedCodec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("count", 1).forGetter(SizedIngredient::count)
            ).apply(sizedIngredientInstance, (items, count) -> new SizedIngredient(Ingredient.of(items.stream()), count)), "Sized ingredient with nbt"
    );

    public static final NamedCodec<AABB> BOX = NamedCodec.DOUBLE_STREAM.comapFlatMap(stream -> {
        double[] arr = stream.toArray();
        if(arr.length == 3)
            return DataResult.success(new AABB(arr[0], arr[1], arr[2], arr[0], arr[1], arr[2]));
        else if(arr.length == 6)
            return DataResult.success(new AABB(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]));
        else
            return DataResult.error(() -> Arrays.toString(arr) + " is not an array of 3 or 6 elements");
    }, aabb -> DoubleStream.of(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ), "Box");

    public static <T> NamedCodec<TagKey<T>> tagKey(ResourceKey<Registry<T>> registry) {
        return RESOURCE_LOCATION.xmap(rl -> TagKey.create(registry, rl), TagKey::location, "Tag: " + registry.location());
    }

    public static <T> NamedCodec<Either<TagKey<T>, Holder<T>>> registryValueOrTag(Registry<T> registry) {
        return NamedCodec.STRING.comapFlatMap(s -> {
            if(s.startsWith("#")) {
                try {
                    TagKey<T> key = TagKey.create(registry.key(), ResourceLocation.parse(s.substring(1)));
                    if(CustomMachineJsonReloadListener.context != null && CustomMachineJsonReloadListener.context.getTag(key).isEmpty())
                        return DataResult.error(() -> "Invalid tag: " + s);
                    return DataResult.success(Either.<TagKey<T>, Holder<T>>left(key));
                } catch (ResourceLocationException e) {
                    return DataResult.error(e::getMessage);
                }
            } else {
                try {
                    Optional<Reference<T>> ref = registry.getHolder(ResourceLocation.parse(s));
                    return ref.map(reference -> DataResult.success(Either.<TagKey<T>, Holder<T>>right(reference))).orElse(DataResult.error(() -> "Invalid item: " + s));
                } catch (ResourceLocationException e) {
                    return DataResult.error(e::getMessage);
                }
            }
        }, either -> either.map(key -> "#" + key.location(), holder -> holder.getKey().location().toString()), "Value or Tag: " + registry.key().location());
    }

    private static DataResult<ResourceLocation> decodeResourceLocation(String encoded) {
        try {
            return DataResult.success(ResourceLocation.parse(encoded));
        } catch (ResourceLocationException e) {
            return DataResult.error(e::getMessage);
        }
    }

    private static DataResult<Character> decodeCharacter(String encoded) {
        if(encoded.length() != 1)
            return DataResult.error(() -> "Invalid character : \"" + encoded + "\" must be a single character !");
        return DataResult.success(encoded.charAt(0));
    }
}
