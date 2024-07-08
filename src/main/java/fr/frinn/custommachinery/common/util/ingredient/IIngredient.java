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
