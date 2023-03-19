package fr.frinn.custommachinery.common.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.guielement.TextGuiElement;
import fr.frinn.custommachinery.impl.codec.EnhancedEitherCodec;
import fr.frinn.custommachinery.impl.codec.EnhancedListCodec;
import fr.frinn.custommachinery.impl.codec.EnumCodec;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.util.ModelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public class Codecs {

    public static final NamedCodec<CompoundTag> COMPOUND_NBT_CODEC               = NamedCodec.STRING.comapFlatMap(Codecs::decodeCompoundNBT, CompoundTag::toString, "NBT");

    public static final NamedCodec<TextGuiElement.Alignment> ALIGNMENT_CODEC                 = fromEnum(TextGuiElement.Alignment.class);
    public static final NamedCodec<ProgressBarGuiElement.Orientation> PROGRESS_DIRECTION       = fromEnum(ProgressBarGuiElement.Orientation.class);

    public static final NamedCodec<BlockPos> BLOCK_POS              = NamedCodec.of(BlockPos.CODEC, "Block Position");
    public static final NamedCodec<AABB> AABB_CODEC                 = NamedCodec.DOUBLE_STREAM.comapFlatMap(stream -> validateDoubleStreamSize(stream, 6).map(array -> new AABB(array[0], array[1], array[2], array[3], array[4], array[5])), box -> DoubleStream.of(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ), "Box");
    public static final NamedCodec<AABB> BOX_CODEC                  = either(BLOCK_POS, AABB_CODEC, "Box").xmap(either -> either.map(pos -> new AABB(pos, pos), Function.identity()), Either::right, "Box");
    public static final NamedCodec<ModelLocation> BLOCK_MODEL_CODEC = either(ModelLocation.CODEC, PartialBlockState.CODEC, "Block Model").xmap(either -> either.map(Function.identity(), PartialBlockState::getModelLocation), Either::left, "Block model location");
    public static final NamedCodec<ModelLocation> ITEM_MODEL_CODEC  = either(RegistrarCodec.ITEM, ModelLocation.CODEC, "Item Model").xmap(either -> either.map(item -> ModelLocation.of(Registry.ITEM.getKey(item)), Function.identity()), Either::right, "Item model location");

    public static <E extends Enum<E>> NamedCodec<E> fromEnum(Class<E> enumClass) {
        return EnumCodec.of(enumClass);
    }

    public static <F, S> NamedCodec<Either<F, S>> either(final NamedCodec<F> first, final NamedCodec<S> second, String name) {
        return EnhancedEitherCodec.of(first, second, name);
    }

    public static <T> NamedCodec<List<T>> list(NamedCodec<T> codec) {
        return EnhancedListCodec.of(codec);
    }

    private static DataResult<CompoundTag> decodeCompoundNBT(String encoded) {
        try {
            return DataResult.success(TagParser.parseTag(encoded));
        } catch (CommandSyntaxException e) {
            return DataResult.error("Not a valid NBT: " + encoded + " " + e.getMessage());
        }
    }

    public static DataResult<double[]> validateDoubleStreamSize(DoubleStream stream, int size) {
        double[] array = stream.limit(size + 1).toArray();
        if (array.length != size) {
            String s = "Input is not a list of " + size + " doubles";
            return array.length >= size ? DataResult.error(s, Arrays.copyOf(array, size)) : DataResult.error(s);
        } else {
            return DataResult.success(array);
        }
    }
}
