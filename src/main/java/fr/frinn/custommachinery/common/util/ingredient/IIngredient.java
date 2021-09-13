package fr.frinn.custommachinery.common.util.ingredient;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface IIngredient<T> extends Predicate<T> {

    Codec<IIngredient<Item>> ITEM = Codecs.either(ItemIngredient.CODEC, ItemTagIngredient.CODEC, "Item Ingredient").flatComapMap(
            either -> either.map(Function.identity(), Function.identity()),
            ingredient -> {
                if(ingredient instanceof ItemIngredient)
                    return DataResult.success(Either.left((ItemIngredient)ingredient));
                else if(ingredient instanceof ItemTagIngredient)
                    return DataResult.success(Either.right((ItemTagIngredient)ingredient));
                return DataResult.error(String.format("Item Ingredient : %s is not an item nor a tag !", ingredient));
            }
    );

    Codec<IIngredient<Fluid>> FLUID = Codecs.either(FluidIngredient.CODEC, FluidTagIngredient.CODEC, "Fluid Ingredient").flatComapMap(
            either -> either.map(Function.identity(), Function.identity()),
            ingredient -> {
                if(ingredient instanceof FluidIngredient)
                    return DataResult.success(Either.left((FluidIngredient)ingredient));
                else if(ingredient instanceof FluidTagIngredient)
                    return DataResult.success(Either.right((FluidTagIngredient)ingredient));
                return DataResult.error(String.format("Fluid Ingredient : %s is not an fluid nor a tag !", ingredient));
            }
    );

    List<T> getAll();
}
