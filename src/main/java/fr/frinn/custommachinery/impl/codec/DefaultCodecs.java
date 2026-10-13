package fr.frinn.custommachinery.impl.codec;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.machine.CustomMachineJsonReloadListener;
import net.minecraft.IdentifierException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class DefaultCodecs {

    public static final NamedCodec<Identifier> IDENTIFIER = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeResourceLocation, Identifier::toString, "Resource location");
    public static final NamedCodec<Character> CHARACTER = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeCharacter, Object::toString, "Character");
    public static final NamedCodec<BlockState> BLOCKSTATE = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeBlockState, BlockStateParser::serialize, "BlockState");
    public static final NamedCodec<Integer> COLOR_ARGB_ARRAY = NamedCodec.INT_STREAM.comapFlatMap(stream -> {
        int[] arr = stream.toArray();
        if(arr.length == 4)
            return DataResult.success(ARGB.color(arr[0], arr[1], arr[2], arr[3]));
        else if(arr.length == 3)
            return DataResult.success(ARGB.color(255, arr[0], arr[1], arr[2]));
        else
            return DataResult.error(() -> Arrays.toString(arr) + " is not an array of 4 or 3 elements");
    }, color -> IntStream.of(ARGB.alpha(color), ARGB.red(color), ARGB.green(color), ARGB.blue(color)), "ARGB color array");
    public static final NamedCodec<Integer> COLOR_ARGB = NamedCodec.either(COLOR_ARGB_ARRAY, NamedCodec.INT).xmap(either -> either.map(Function.identity(), Function.identity()), Either::right, "ARGB color");

    public static final NamedCodec<SoundEvent> SOUND_EVENT = IDENTIFIER.xmap(SoundEvent::createVariableRangeEvent, SoundEvent::location, "Sound event");

    public static final NamedCodec<Direction> DIRECTION = NamedCodec.enumCodec(Direction.class);

    public static final NamedCodec<ItemStack> ITEM_OR_STACK = NamedCodec.either(RegistryCodecs.ITEM, NamedCodec.of(ItemStack.OPTIONAL_CODEC), "ItemStack").xmap(either -> either.map(Item::getDefaultInstance, Function.identity()), Either::right, "Item Stack");

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

    public static final NamedCodec<CompoundTag> NBT = NamedCodec.of(CompoundTag.CODEC, "Compound tag");

    public static <T> NamedCodec<TagKey<T>> tagKey(ResourceKey<Registry<T>> registry) {
        return IDENTIFIER.xmap(rl -> TagKey.create(registry, rl), TagKey::location, "Tag: " + registry.identifier());
    }

    public static <T> NamedCodec<Either<TagKey<T>, Holder<T>>> registryValueOrTag(Registry<T> registry) {
        return NamedCodec.STRING.comapFlatMap(s -> {
            if(s.startsWith("#")) {
                try {
                    TagKey<T> key = TagKey.create(registry.key(), Identifier.parse(s.substring(1)));
                    if(CustomMachineJsonReloadListener.context != null && CustomMachineJsonReloadListener.context.getTag(key).isEmpty())
                        return DataResult.error(() -> "Invalid tag: " + s);
                    return DataResult.success(Either.left(key));
                } catch (IdentifierException e) {
                    return DataResult.error(e::getMessage);
                }
            } else {
                try {
                    Optional<Reference<T>> ref = registry.get(Identifier.parse(s));
                    return ref.map(reference -> DataResult.success(Either.<TagKey<T>, Holder<T>>right(reference))).orElse(DataResult.error(() -> "Invalid item: " + s));
                } catch (IdentifierException e) {
                    return DataResult.error(e::getMessage);
                }
            }
        }, either -> either.map(key -> "#" + key.location(), Holder::getRegisteredName), "Value or Tag: " + registry.key().identifier());
    }

    private static DataResult<Identifier> decodeResourceLocation(String encoded) {
        try {
            return DataResult.success(Identifier.parse(encoded));
        } catch (IdentifierException e) {
            return DataResult.error(e::getMessage);
        }
    }

    private static DataResult<Character> decodeCharacter(String encoded) {
        if(encoded.length() != 1)
            return DataResult.error(() -> "Invalid character : \"" + encoded + "\" must be a single character !");
        return DataResult.success(encoded.charAt(0));
    }

    private static DataResult<BlockState> decodeBlockState(String encoded) {
        try {
            return DataResult.success(BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, new StringReader(encoded), false).blockState());
        } catch (CommandSyntaxException e) {
            return DataResult.error(() -> "Invalid blockstate: " + encoded + "\n" + e.getMessage());
        }
    }
}
