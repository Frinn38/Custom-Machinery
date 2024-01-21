package fr.frinn.custommachinery.impl.codec;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public class DefaultCodecs {

    public static final NamedCodec<ResourceLocation> RESOURCE_LOCATION = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeResourceLocation, ResourceLocation::toString, "Resource location");
    public static final NamedCodec<Character> CHARACTER = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeCharacter, Object::toString, "Character");
    public static final NamedCodec<CompoundTag> NBT_FROM_STRING = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeNbtFromString, CompoundTag::toString, "NBT from String");
    public static final NamedCodec<CompoundTag> NBT_FROM_JSON = NamedCodec.of(CompoundTag.CODEC, "NBT from JSON");
    public static final NamedCodec<CompoundTag> COMPOUND_TAG = NamedCodec.either(NBT_FROM_STRING, NBT_FROM_JSON, "Compound nbt").xmap(either -> either.map(Function.identity(), Function.identity()), Either::left, "Compound nbt");

    public static final NamedCodec<SoundEvent> SOUND_EVENT = RESOURCE_LOCATION.xmap(SoundEvent::new, SoundEvent::getLocation, "Sound event");

    public static final NamedCodec<Direction> DIRECTION = NamedCodec.enumCodec(Direction.class);

    public static final NamedCodec<ItemStack> ITEM_STACK = NamedCodec.record(itemStackInstance ->
            itemStackInstance.group(
                    RegistrarCodec.ITEM.fieldOf("id").forGetter(ItemStack::getItem),
                    NamedCodec.INT.optionalFieldOf("Count", 1).forGetter(ItemStack::getCount),
                    COMPOUND_TAG.optionalFieldOf("tag").forGetter(stack -> Optional.ofNullable(stack.getTag()))
            ).apply(itemStackInstance, (item, count, nbt) -> {
                ItemStack stack = item.getDefaultInstance();
                stack.setCount(count);
                nbt.ifPresent(stack::setTag);
                return stack;
            }), "Item stack"
    );

    public static final NamedCodec<ItemStack> ITEM_OR_STACK = NamedCodec.either(RegistrarCodec.ITEM, ITEM_STACK).xmap(either -> either.map(Item::getDefaultInstance, Function.identity()), Either::right, "Item Stack");

    public static final NamedCodec<Ingredient> INGREDIENT = NamedCodec.fromJson(Ingredient::fromJson, Ingredient::toJson, "Ingredient");

    public static final NamedCodec<AABB> BOX = NamedCodec.DOUBLE_STREAM.comapFlatMap(stream -> {
        double[] arr = stream.toArray();
        if(arr.length == 3)
            return DataResult.success(new AABB(arr[0], arr[1], arr[2], arr[0], arr[1], arr[2]));
        else if(arr.length == 6)
            return DataResult.success(new AABB(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]));
        else
            return DataResult.error(Arrays.toString(arr) + " is not an array of 3 or 6 elements");
    }, aabb -> DoubleStream.of(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ), "Box");

    public static <T> NamedCodec<TagKey<T>> tagKey(ResourceKey<Registry<T>> registry) {
        return RESOURCE_LOCATION.xmap(rl -> TagKey.create(registry, rl), TagKey::location, "Tag: " + registry.location());
    }

    private static DataResult<ResourceLocation> decodeResourceLocation(String encoded) {
        try {
            return DataResult.success(new ResourceLocation(encoded));
        } catch (ResourceLocationException e) {
            return DataResult.error(e.getMessage());
        }
    }

    private static DataResult<Character> decodeCharacter(String encoded) {
        if(encoded.length() != 1)
            return DataResult.error("Invalid character : \"" + encoded + "\" must be a single character !");
        return DataResult.success(encoded.charAt(0));
    }

    private static DataResult<CompoundTag> decodeNbtFromString(String encoded) {
        TagParser parser = new TagParser(new StringReader(encoded));
        try {
            return DataResult.success(parser.readStruct());
        } catch (CommandSyntaxException e) {
            return DataResult.error(e.getMessage());
        }
    }
}
