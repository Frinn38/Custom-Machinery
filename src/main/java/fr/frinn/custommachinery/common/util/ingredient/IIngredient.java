package fr.frinn.custommachinery.common.util.ingredient;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface IIngredient<O> extends Predicate<O> {

    NamedCodec<IIngredient<Item>> ITEM = new NamedCodec<>() {
        @Override
        public <T> DataResult<Pair<IIngredient<Item>, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<MapLike<T>> mapResult = ops.getMap(input);
            if(mapResult.result().isPresent()) {
                MapLike<T> map = mapResult.result().get();
                T item = map.get("item");
                if(item != null)
                    return RegistrarCodec.ITEM.read(ops, item).map(i -> Pair.of(new ItemIngredient(i), ops.empty()));
                T tag = map.get("tag");
                if(tag != null)
                    return DefaultCodecs.tagKey(Registries.ITEM).read(ops, tag).map(t -> Pair.of(ItemTagIngredient.create(t), ops.empty()));
                return DataResult.error(() -> "Couldn't get an item ingredient from: " + map);
            }
            DataResult<String> result = ops.getStringValue(input);
            if(result.result().isPresent()) {
                String s = result.result().get();
                if(s.startsWith("#")) {
                    try {
                        return DataResult.success(Pair.of(ItemTagIngredient.create(s), ops.empty()));
                    } catch (IllegalArgumentException e) {
                        return DataResult.error(e::getMessage);
                    }
                }
                try {
                    return DataResult.success(Pair.of(new ItemIngredient(BuiltInRegistries.ITEM.get(ResourceLocation.parse(s))), ops.empty()));
                } catch (ResourceLocationException e) {
                    return DataResult.error(() -> "Invalid item ID: " + e.getMessage());
                }
            }
            return DataResult.error(() -> "Unable to get an item ingredient from: " + input);
        }

        @Override
        public <T> DataResult<T> encode(DynamicOps<T> ops, IIngredient<Item> input, T prefix) {
            if(input instanceof ItemIngredient ingredient)
                return ops.mergeToPrimitive(prefix, ops.createString(ingredient.toString()));
            else if(input instanceof ItemTagIngredient ingredient)
                return ops.mergeToPrimitive(prefix, ops.createString(ingredient.toString()));
            return DataResult.error(() -> String.format("Item Ingredient: %s is not an item nor a tag !", input));
        }

        @Override
        public String name() {
            return "Item ingredient";
        }
    };

    NamedCodec<IIngredient<Fluid>> FLUID = NamedCodec.either(FluidIngredient.CODEC, FluidTagIngredient.CODEC, "Fluid Ingredient").flatComapMap(
            either -> either.map(Function.identity(), Function.identity()),
            ingredient -> {
                if(ingredient instanceof FluidIngredient)
                    return DataResult.success(Either.left((FluidIngredient)ingredient));
                else if(ingredient instanceof FluidTagIngredient)
                    return DataResult.success(Either.right((FluidTagIngredient)ingredient));
                return DataResult.error(() -> String.format("Fluid Ingredient : %s is not a fluid nor a tag !", ingredient));
            },
            "Fluid ingredient"
    );

    NamedCodec<IIngredient<PartialBlockState>> BLOCK = NamedCodec.either(BlockIngredient.CODEC, BlockTagIngredient.CODEC, "Block Ingredient").flatComapMap(
            either -> either.map(Function.identity(), Function.identity()),
            ingredient -> {
                if(ingredient instanceof BlockIngredient)
                    return DataResult.success(Either.left((BlockIngredient)ingredient));
                else if(ingredient instanceof BlockTagIngredient)
                    return DataResult.success(Either.right((BlockTagIngredient)ingredient));
                return DataResult.error(() -> String.format("Block Ingredient : %s is not a block nor a tag !", ingredient));
            },
            "Block ingredient"
    );

    List<O> getAll();
}
