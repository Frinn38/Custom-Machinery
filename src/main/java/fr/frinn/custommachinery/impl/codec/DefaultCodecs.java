package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public class DefaultCodecs {

    public static final NamedCodec<ResourceLocation> RESOURCE_LOCATION = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeResourceLocation, ResourceLocation::toString, "Resource location");
    public static final NamedCodec<Character> CHARACTER = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeCharacter, Object::toString, "Character");

    public static final NamedCodec<SoundEvent> SOUND_EVENT = RESOURCE_LOCATION.xmap(SoundEvent::createVariableRangeEvent, SoundEvent::getLocation, "Sound event");

    public static final NamedCodec<Direction> DIRECTION = NamedCodec.enumCodec(Direction.class);

    public static final NamedCodec<ItemStack> ITEM_OR_STACK = NamedCodec.either(RegistrarCodec.ITEM, NamedCodec.of(ItemStack.OPTIONAL_CODEC), "ItemStack").xmap(either -> either.map(Item::getDefaultInstance, Function.identity()), Either::right, "Item Stack");

    public static final NamedCodec<Ingredient> INGREDIENT = NamedCodec.of(Ingredient.CODEC, "Ingredient");

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
