package fr.frinn.custommachinery.common.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.codec.EnhancedEitherCodec;
import fr.frinn.custommachinery.api.codec.EnhancedListCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.requirement.BlockRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.DropRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.EntityRequirement;
import fr.frinn.custommachinery.common.data.MachineLocation;
import fr.frinn.custommachinery.common.data.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.data.gui.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.data.gui.TextGuiElement;
import fr.frinn.custommachinery.common.data.upgrade.RecipeModifier;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public class Codecs {

    public static final Codec<DoubleStream> DOUBLE_STREAM = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<DoubleStream> read(final DynamicOps<T> ops, final T input) {
            return ops.getStream(input).flatMap(stream -> {
                final List<T> list = stream.toList();
                if (list.stream().allMatch(element -> ops.getNumberValue(element).result().isPresent()))
                    return DataResult.success(list.stream().mapToDouble(element -> ops.getNumberValue(element).result().get().doubleValue()));
                return DataResult.error("Some elements are not doubles: " + input);
            });
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final DoubleStream value) {
            return ops.createList(value.mapToObj(ops::createDouble));
        }

        @Override
        public String toString() {
            return "DoubleStream";
        }
    };

    public static Codec<Long> longRange(final long minInclusive, final long maxInclusive) {
        final Function<Long, DataResult<Long>> checker = checkRange(minInclusive, maxInclusive);
        return Codec.LONG.flatXmap(checker, checker);
    }

    public static final Codec<PositionComparator> POSITION_COMPARATOR_CODEC = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodePositionComparator, PositionComparator::toString), "Position Comparator");
    public static final Codec<TimeComparator> TIME_COMPARATOR_CODEC         = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodeTimeComparator, TimeComparator::toString), "Time Comparator");
    public static final Codec<CompoundTag> COMPOUND_NBT_CODEC               = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodeCompoundNBT, CompoundTag::toString), "NBT");
    public static final Codec<Character> CHARACTER_CODEC                    = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodeCharacter, Object::toString), "Character");
    public static final Codec<PartialBlockState> PARTIAL_BLOCK_STATE_CODEC  = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodePartialBlockState, PartialBlockState::toString), "Block State");
    //public static final Codec<ToolType> TOOL_TYPE_CODEC                     = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodeToolType, ToolType::getName), "Tool Type");
    public static final Codec<ResourceLocation> RESOURCE_LOCATION_CODEC     = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodeResourceLocation, ResourceLocation::toString), "Resource Location");

    public static final Codec<ComparatorMode> COMPARATOR_MODE_CODEC         = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodeComparatorMode, ComparatorMode::getPrefix), "Comparator Mode");

    public static final Codec<MachineStatus> STATUS_CODEC                               = fromEnum(MachineStatus.class);
    public static final Codec<ComponentIOMode> COMPONENT_MODE_CODEC                     = fromEnum(ComponentIOMode.class);
    public static final Codec<RequirementIOMode> REQUIREMENT_MODE_CODEC                 = fromEnum(RequirementIOMode.class);
    public static final Codec<MachineLocation.Loader> LOADER_CODEC                      = fromEnum(MachineLocation.Loader.class);
    public static final Codec<TextGuiElement.Alignment> ALIGNMENT_CODEC                 = fromEnum(TextGuiElement.Alignment.class);
    public static final Codec<CraftingManager.PHASE> PHASE_CODEC                        = fromEnum(CraftingManager.PHASE.class);
    public static final Codec<WeatherMachineComponent.WeatherType> WEATHER_TYPE_CODEC   = fromEnum(WeatherMachineComponent.WeatherType.class);
    public static final Codec<EntityRequirement.ACTION> ENTITY_REQUIREMENT_ACTION_CODEC = fromEnum(EntityRequirement.ACTION.class);
    public static final Codec<BlockRequirement.ACTION> BLOCK_REQUIREMENT_ACTION_CODEC   = fromEnum(BlockRequirement.ACTION.class);
    public static final Codec<RecipeModifier.OPERATION> MODIFIER_OPERATION_CODEC        = fromEnum(RecipeModifier.OPERATION.class);
    public static final Codec<ProgressBarGuiElement.Direction> PROGRESS_DIRECTION       = fromEnum(ProgressBarGuiElement.Direction.class);
    public static final Codec<DropRequirement.Action> DROP_REQUIREMENT_ACTION_CODEC     = fromEnum(DropRequirement.Action.class);

    public static final Codec<BlockPos> BLOCK_POS                 = CodecLogger.namedCodec(BlockPos.CODEC, "Block Position");
    public static final Codec<AABB> AABB_CODEC           = CodecLogger.namedCodec(DOUBLE_STREAM.comapFlatMap(stream -> validateDoubleStreamSize(stream, 6).map(array -> new AABB(array[0], array[1], array[2], array[3], array[4], array[5])), box -> DoubleStream.of(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)), "Box");
    public static final Codec<AABB> BOX_CODEC            = either(BLOCK_POS, AABB_CODEC, "Box").xmap(either -> either.map(pos -> new AABB(pos, pos), Function.identity()), Either::right);
    public static final Codec<ResourceLocation> BLOCK_MODEL_CODEC = either(RESOURCE_LOCATION_CODEC, PARTIAL_BLOCK_STATE_CODEC, "Block Model").xmap(either -> either.map(Function.identity(), PartialBlockState::getModelLocation), Either::left);
    public static final Codec<ResourceLocation> ITEM_MODEL_CODEC  = either(ForgeRegistries.ITEMS.getCodec(), RESOURCE_LOCATION_CODEC, "Item Model").xmap(either -> either.map(Item::getRegistryName, Function.identity()), Either::right);
    public static final Codec<VoxelShape> VOXEL_SHAPE_CODEC       = either(Codecs.PARTIAL_BLOCK_STATE_CODEC, Codecs.list(Codecs.BOX_CODEC), "Machine Shape").comapFlatMap(either -> either.map(Codecs::decodeFromBlock, Codecs::decodeFromAABBList), shape -> Either.right(shape.toAabbs()));

    public static <E extends Enum<E>> Codec<E> fromEnum(Class<E> enumClass) {
        return new Codec<E>() {
            @Override
            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> ops, T input) {
                return ops.getStringValue(input).flatMap(s -> {
                    try {
                        return DataResult.success(Pair.of(Enum.valueOf(enumClass, s.toUpperCase(Locale.ENGLISH)), input));
                    } catch (IllegalArgumentException e) {
                        return DataResult.error(String.format("Not a valid %s: %s%n%s", enumClass.getSimpleName(), s, e.getMessage()));
                    }
                });
            }

            @Override
            public <T> DataResult<T> encode(E input, DynamicOps<T> ops, T prefix) {
                T string = ops.createString(input.toString());
                return ops.mergeToPrimitive(prefix, string);
            }

            @Override
            public String toString() {
                return enumClass.getSimpleName();
            }
        };
    }

    public static <F, S> Codec<Either<F, S>> either(final Codec<F> first, final Codec<S> second, String name) {
        return new EnhancedEitherCodec<>(first, second, name);
    }

    public static <T> Codec<List<T>> list(Codec<T> codec) {
        return new EnhancedListCodec<>(codec);
    }

    public static <E> Codec<Tag<E>> tagCodec(Function<ResourceLocation, Tag<E>> tagGetter, Function<Tag<E>, ResourceLocation> idGetter) {
        return Codec.STRING.comapFlatMap(string -> {
            if(string.startsWith("#"))
                string = string.substring(1);
            if(!Utils.isResourceNameValid(string))
                return DataResult.error(String.format("Invalid tag : %s is not a valid tag id !", string));
            Tag<E> tag = tagGetter.apply(new ResourceLocation(string));
            if(tag == null)
                return DataResult.error("Unknown tag : " + string);
            return DataResult.success(tag);
        }, tag -> idGetter.apply(tag).toString());
    }

    public static <T> Codec<ResourceKey<T>> registryKeyCodec(ResourceKey<Registry<T>> keyRegistry) {
        return ResourceLocation.CODEC.xmap(loc -> ResourceKey.create(keyRegistry, loc), ResourceKey::location);
    }

    private static DataResult<PositionComparator> decodePositionComparator(String encoded) {
        try {
            return DataResult.success(new PositionComparator(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error(String.format("Not a valid Position Comparator: %s%n%s", encoded, e.getMessage()));
        }
    }

    private static DataResult<TimeComparator> decodeTimeComparator(String encoded) {
        try {
            return DataResult.success(new TimeComparator(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error(String.format("Not a valid Time Comparator: %s%n%s", encoded, e.getMessage()));
        }
    }

    private static DataResult<CompoundTag> decodeCompoundNBT(String encoded) {
        try {
            return DataResult.success(TagParser.parseTag(encoded));
        } catch (CommandSyntaxException e) {
            return DataResult.error("Not a valid NBT: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<Character> decodeCharacter(String encoded) {
        if(encoded.length() != 1)
            return DataResult.error("Invalid character : \"" + encoded + "\" must be a single character !");
        return DataResult.success(encoded.charAt(0));
    }

    private static DataResult<PartialBlockState> decodePartialBlockState(String encoded) {
        StringReader reader = new StringReader(encoded);
        try {
            BlockStateParser parser = new BlockStateParser(reader, false).parse(true);
            return DataResult.success(new PartialBlockState(parser.getState(), Lists.newArrayList(parser.getProperties().keySet()), parser.getNbt()));
        } catch (CommandSyntaxException exception) {
            return DataResult.error(exception.getMessage());
        }
    }
/*
    private static DataResult<ToolType> decodeToolType(String encoded) {
        try {
            return DataResult.success(ToolType.get(encoded.toLowerCase(Locale.ENGLISH)));
        } catch (IllegalArgumentException e) {
            return DataResult.error(e.getMessage());
        }
    }
*/
    private static DataResult<ResourceLocation> decodeResourceLocation(String encoded) {
        try {
            if(encoded.contains("#"))
                return DataResult.success(new ModelResourceLocation(encoded));
            else
                return DataResult.success(new ResourceLocation(encoded));
        } catch (Exception e) {
            return DataResult.error(e.getMessage());
        }
    }

    public static DataResult<VoxelShape> decodeFromBlock(PartialBlockState state) {
        try {
            return DataResult.success(state.getBlockState().getShape(null, null));
        } catch (Exception e) {
            return DataResult.error("Impossible to get shape from block : " + state);
        }
    }

    public static DataResult<VoxelShape> decodeFromAABBList(List<AABB> boxes) {
        VoxelShape shape = Shapes.empty();
        for(AABB box : boxes) {
            VoxelShape partial = Shapes.create(box);
            shape = Shapes.joinUnoptimized(shape, partial, BooleanOp.OR);
        }
        return DataResult.success(shape);
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

    public static DataResult<ComparatorMode> decodeComparatorMode(String encoded) {
        try {
            return DataResult.success(ComparatorMode.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Invalid Comparator mode : " + encoded);
        }
    }

    public static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRange(final N minInclusive, final N maxInclusive) {
        return value -> {
            if (value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error("Value " + value + " outside of range [" + minInclusive + ":" + maxInclusive + "]", value);
        };
    }
}
