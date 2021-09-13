package fr.frinn.custommachinery.common.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.frinn.custommachinery.api.components.ComponentIOMode;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.api.utils.EnhancedEitherCodec;
import fr.frinn.custommachinery.api.utils.EnhancedListCodec;
import fr.frinn.custommachinery.api.utils.RegistryCodec;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.requirements.BlockRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.EntityRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.RequirementType;
import fr.frinn.custommachinery.common.data.MachineLocation;
import fr.frinn.custommachinery.common.data.component.ItemComponentVariant;
import fr.frinn.custommachinery.common.data.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.data.gui.GuiElementType;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import fr.frinn.custommachinery.common.data.gui.TextGuiElement;
import fr.frinn.custommachinery.common.data.upgrade.RecipeModifier;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.block.Block;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Codecs {

    public static final Codec<GuiElementType<? extends IGuiElement>> GUI_ELEMENT_TYPE      = RegistryCodec.of(Registration.GUI_ELEMENT_TYPE_REGISTRY.get());
    public static final Codec<RequirementType<? extends IRequirement<?>>> REQUIREMENT_TYPE = RegistryCodec.of(Registration.REQUIREMENT_TYPE_REGISTRY.get());

    public static final Codec<PositionComparator> POSITION_COMPARATOR_CODEC             = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodePositionComparator, PositionComparator::toString), "Position Comparator");
    public static final Codec<TimeComparator> TIME_COMPARATOR_CODEC                     = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodeTimeComparator, TimeComparator::toString), "Time Comparator");
    public static final Codec<CompoundNBT> COMPOUND_NBT_CODEC                           = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodeCompoundNBT, CompoundNBT::toString), "NBT");
    public static final Codec<Character> CHARACTER_CODEC                                = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodeCharacter, Object::toString), "Character");
    public static final Codec<PartialBlockState> PARTIAL_BLOCK_STATE_CODEC              = CodecLogger.namedCodec(Codec.STRING.comapFlatMap(Codecs::decodePartialBlockState, PartialBlockState::toString), "Block State");

    public static final Codec<ItemComponentVariant> ITEM_COMPONENT_VARIANT_CODEC = CodecLogger.namedCodec(ResourceLocation.CODEC.comapFlatMap(Codecs::decodeItemComponentVariant, ItemComponentVariant::getId), "Item Component Variant");

    public static final Codec<ITag.INamedTag<Item>> ITEM_TAG_CODEC   = CodecLogger.namedCodec(tagCodec(ItemTags::createOptional), "Item Tag");
    public static final Codec<ITag.INamedTag<Fluid>> FLUID_TAG_CODEC = CodecLogger.namedCodec(tagCodec(FluidTags::createOptional), "Fluid Tag");
    public static final Codec<ITag.INamedTag<Block>> BLOCK_TAG_CODEC = CodecLogger.namedCodec(tagCodec(BlockTags::createOptional), "Block Tag");

    public static final Codec<BlockPos> BLOCK_POS       = CodecLogger.namedCodec(BlockPos.CODEC, "Block Position");
    public static final Codec<AxisAlignedBB> AABB_CODEC = CodecLogger.namedCodec(Codec.INT_STREAM.comapFlatMap(stream -> Util.validateIntStreamSize(stream, 6).map(array -> new AxisAlignedBB(array[0], array[1], array[2], array[3], array[4], array[5])), box -> IntStream.of((int)box.minX, (int)box.minY, (int)box.minZ, (int)box.maxX, (int)box.maxY, (int)box.maxZ)), "Box");
    public static final Codec<AxisAlignedBB> BOX_CODEC  = either(BLOCK_POS, AABB_CODEC, "Box").xmap(either -> either.map(pos -> new AxisAlignedBB(pos, pos), Function.identity()), Either::right);

    public static final Codec<MachineStatus> STATUS_CODEC                               = fromEnum(MachineStatus.class);
    public static final Codec<ComponentIOMode> COMPONENT_MODE_CODEC                     = fromEnum(ComponentIOMode.class);
    public static final Codec<IRequirement.MODE> REQUIREMENT_MODE_CODEC                 = fromEnum(IRequirement.MODE.class);
    public static final Codec<MachineLocation.Loader> LOADER_CODEC                      = fromEnum(MachineLocation.Loader.class);
    public static final Codec<TextGuiElement.Alignment> ALIGNMENT_CODEC                 = fromEnum(TextGuiElement.Alignment.class);
    public static final Codec<CraftingManager.PHASE> PHASE_CODEC                        = fromEnum(CraftingManager.PHASE.class);
    public static final Codec<WeatherMachineComponent.WeatherType> WEATHER_TYPE_CODEC   = fromEnum(WeatherMachineComponent.WeatherType.class);
    public static final Codec<ComparatorMode> COMPARATOR_MODE_CODEC                     = fromEnum(ComparatorMode.class);
    public static final Codec<EntityRequirement.ACTION> ENTITY_REQUIREMENT_ACTION_CODEC = fromEnum(EntityRequirement.ACTION.class);
    public static final Codec<BlockRequirement.ACTION> BLOCK_REQUIREMENT_ACTION_CODEC   = fromEnum(BlockRequirement.ACTION.class);
    public static final Codec<RecipeModifier.OPERATION> MODIFIER_OPERATION_CODEC        = fromEnum(RecipeModifier.OPERATION.class);

    public static final Codec<ResourceLocation> BLOCK_MODEL_CODEC = either(PARTIAL_BLOCK_STATE_CODEC, ResourceLocation.CODEC, "Block Model").xmap(either -> either.map(PartialBlockState::getModelLocation, Function.identity()), Either::right);
    public static final Codec<ResourceLocation> ITEM_MODEL_CODEC  = either(RegistryCodec.ITEM, ResourceLocation.CODEC, "Item Model").xmap(either -> either.map(Item::getRegistryName, Function.identity()), Either::right);

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

    public static <E> Codec<ITag.INamedTag<E>> tagCodec(Function<ResourceLocation, ITag.INamedTag<E>> tagBuilder) {
        return Codec.STRING.comapFlatMap(string -> {
            if(string.startsWith("#"))
                string = string.substring(1);
            if(!Utils.isResourceNameValid(string))
                return DataResult.error(String.format("Invalid tag : %s is not a valid tag id !", string));
            return DataResult.success(tagBuilder.apply(new ResourceLocation(string)));
        }, tag -> tag.getName().toString());
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

    private static DataResult<CompoundNBT> decodeCompoundNBT(String encoded) {
        try {
            return DataResult.success(JsonToNBT.getTagFromJson(encoded));
        } catch (CommandSyntaxException e) {
            return DataResult.error("Not a valid NBT: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<ItemComponentVariant> decodeItemComponentVariant(ResourceLocation encoded) {
        try {
            return DataResult.success(Objects.requireNonNull(ItemComponentVariant.getVariant(encoded)));
        } catch (NullPointerException e) {
            return DataResult.error("Not a valid Item component variant: " + encoded + " " + e.getMessage());
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
}
