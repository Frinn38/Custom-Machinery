package fr.frinn.custommachinery.common.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.components.ComponentIOMode;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.api.utils.RegistryCodec;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.requirements.BlockRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.EntityRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.RequirementType;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.MachineLocation;
import fr.frinn.custommachinery.common.data.component.ItemComponentVariant;
import fr.frinn.custommachinery.common.data.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.data.gui.GuiElementType;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import fr.frinn.custommachinery.common.data.gui.TextGuiElement;
import fr.frinn.custommachinery.common.data.upgrade.RecipeModifier;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.stream.IntStream;

public class Codecs {

    public static final Codec<Item> ITEM_CODEC                          = new RegistryCodec<>(ForgeRegistries.ITEMS).stable();
    public static final Codec<Block> BLOCK_CODEC                        = new RegistryCodec<>(ForgeRegistries.BLOCKS).stable();
    public static final Codec<EntityType<?>> ENTITY_TYPE_CODEC          = new RegistryCodec<>(ForgeRegistries.ENTITIES).stable();
    public static final Codec<Effect> EFFECT_CODEC                      = new RegistryCodec<>(ForgeRegistries.POTIONS).stable();
    public static final Codec<Fluid> FLUID_CODEC                        = new RegistryCodec<>(ForgeRegistries.FLUIDS).stable();

    public static final Codec<ModelResourceLocation> MODEL_RESOURCE_LOCATION_CODEC      = Codec.STRING.comapFlatMap(Codecs::decodeModelResourceLocation, ModelResourceLocation::toString).stable();
    public static final Codec<MachineAppearance.AppearanceType> APPEARANCE_TYPE_CODEC   = Codec.STRING.comapFlatMap(Codecs::decodeAppearanceType, MachineAppearance.AppearanceType::toString).stable();
    public static final Codec<MachineAppearance.LightMode> LIGHT_MODE_CODEC             = Codec.STRING.comapFlatMap(Codecs::decodeLightMode, MachineAppearance.LightMode::toString).stable();
    public static final Codec<ComponentIOMode> COMPONENT_MODE_CODEC                     = Codec.STRING.comapFlatMap(Codecs::decodeMachineComponentMode, ComponentIOMode::toString).stable();
    public static final Codec<IRequirement.MODE> REQUIREMENT_MODE_CODEC                 = Codec.STRING.comapFlatMap(Codecs::decodeRecipeRequirementMode, IRequirement.MODE::toString).stable();
    public static final Codec<MachineLocation.Loader> LOADER_CODEC                      = Codec.STRING.comapFlatMap(Codecs::decodeLoader, MachineLocation.Loader::toString).stable();
    public static final Codec<PositionComparator> POSITION_COMPARATOR_CODEC             = Codec.STRING.comapFlatMap(Codecs::decodePositionComparator, PositionComparator::toString).stable();
    public static final Codec<TimeComparator> TIME_COMPARATOR_CODEC                     = Codec.STRING.comapFlatMap(Codecs::decodeTimeComparator, TimeComparator::toString).stable();
    public static final Codec<TextGuiElement.Alignment> ALIGNMENT_CODEC                 = Codec.STRING.comapFlatMap(Codecs::decodeAlignment, TextGuiElement.Alignment::toString).stable();
    public static final Codec<CraftingManager.PHASE> PHASE_CODEC                        = Codec.STRING.comapFlatMap(Codecs::decodePhase, CraftingManager.PHASE::toString).stable();
    public static final Codec<WeatherMachineComponent.WeatherType> WEATHER_TYPE_CODEC   = Codec.STRING.comapFlatMap(Codecs::decodeWeather, WeatherMachineComponent.WeatherType::toString).stable();
    public static final Codec<ComparatorMode> COMPARATOR_MODE_CODEC                     = Codec.STRING.comapFlatMap(Codecs::decodeComparatorMode, ComparatorMode::toString).stable();
    public static final Codec<CompoundNBT> COMPOUND_NBT_CODEC                           = Codec.STRING.comapFlatMap(Codecs::decodeCompoundNBT, CompoundNBT::toString).stable();
    public static final Codec<EntityRequirement.ACTION> ENTITY_REQUIREMENT_ACTION_CODEC = Codec.STRING.comapFlatMap(Codecs::decodeEntityRequirementAction, EntityRequirement.ACTION::toString).stable();
    public static final Codec<BlockRequirement.ACTION> BLOCK_REQUIREMENT_ACTION_CODEC   = Codec.STRING.comapFlatMap(Codecs::decodeBlockRequirementAction, BlockRequirement.ACTION::toString).stable();
    public static final Codec<RecipeModifier.OPERATION> MODIFIER_OPERATION_CODEC        = Codec.STRING.comapFlatMap(Codecs::decodeModifierOperation, RecipeModifier.OPERATION::toString).stable();
    public static final Codec<Character> CHARACTER_CODEC                                = Codec.STRING.comapFlatMap(Codecs::decodeCharacter, Object::toString).stable();
    public static final Codec<PartialBlockState> PARTIAL_BLOCK_STATE_CODEC              = Codec.STRING.comapFlatMap(Codecs::decodePartialBlockState, PartialBlockState::toString).stable();

    public static final Codec<GuiElementType<? extends IGuiElement>> GUI_ELEMENT_TYPE_CODEC                            = ResourceLocation.CODEC.comapFlatMap(Codecs::decodeGuiElementType, GuiElementType::getRegistryName).stable();
    public static final Codec<RequirementType<? extends IRequirement>> REQUIREMENT_TYPE_CODEC                          = ResourceLocation.CODEC.comapFlatMap(Codecs::decodeRecipeRequirementType, RequirementType::getRegistryName).stable();
    public static final Codec<ItemComponentVariant> ITEM_COMPONENT_VARIANT_CODEC                                       = ResourceLocation.CODEC.comapFlatMap(Codecs::decodeItemComponentVariant, ItemComponentVariant::getId).stable();

    public static final Codec<AxisAlignedBB> BOX_CODEC = Codec.INT_STREAM.comapFlatMap(stream -> Util.validateIntStreamSize(stream, 6).map(array -> new AxisAlignedBB(array[0], array[1], array[2], array[3], array[4], array[5])), box -> IntStream.of((int)box.minX, (int)box.minY, (int)box.minZ, (int)box.maxX, (int)box.maxY, (int)box.maxZ));

    private static DataResult<ModelResourceLocation> decodeModelResourceLocation(String encoded) {
        try {
            return DataResult.success(new ModelResourceLocation(encoded));
        } catch (ResourceLocationException resourcelocationexception) {
            return DataResult.error("Not a valid model resource location: " + encoded + " " + resourcelocationexception.getMessage());
        }
    }

    private static DataResult<MachineAppearance.AppearanceType> decodeAppearanceType(String encoded) {
        try {
            return DataResult.success(MachineAppearance.AppearanceType.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Appearance Type: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<ComponentIOMode> decodeMachineComponentMode(String encoded) {
        try {
            return DataResult.success(ComponentIOMode.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Machine Component Mode: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<MachineAppearance.LightMode> decodeLightMode(String encoded) {
        try {
            return DataResult.success(MachineAppearance.LightMode.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid LightMode: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<IRequirement.MODE> decodeRecipeRequirementMode(String encoded) {
        try {
            return DataResult.success(IRequirement.MODE.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Requirement Mode: " + encoded + " " + e.getMessage());
        }
    }
    private static DataResult<MachineLocation.Loader> decodeLoader(String encoded) {
        try {
            return DataResult.success(MachineLocation.Loader.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Loader: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<PositionComparator> decodePositionComparator(String encoded) {
        try {
            return DataResult.success(new PositionComparator(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Position Comparator: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<TimeComparator> decodeTimeComparator(String encoded) {
        try {
            return DataResult.success(new TimeComparator(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Time Comparator: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<TextGuiElement.Alignment> decodeAlignment(String encoded) {
        try {
            return DataResult.success(TextGuiElement.Alignment.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Alignment: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<GuiElementType<?>> decodeGuiElementType(ResourceLocation encoded) {
        try {
            return DataResult.success(Objects.requireNonNull(Registration.GUI_ELEMENT_TYPE_REGISTRY.get().getValue(encoded)));
        } catch (NullPointerException e) {
            return DataResult.error("Not a valid Gui Element Type: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<MachineComponentType<?>> decodeMachineComponentType(ResourceLocation encoded) {
        try {
            return DataResult.success(Objects.requireNonNull(Registration.MACHINE_COMPONENT_TYPE_REGISTRY.get().getValue(encoded)));
        } catch (NullPointerException e) {
            return DataResult.error("Not a valid Machine Component Type: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<RequirementType<?>> decodeRecipeRequirementType(ResourceLocation encoded) {
        try {
            return DataResult.success(Objects.requireNonNull(Registration.REQUIREMENT_TYPE_REGISTRY.get().getValue(encoded)));
        } catch (NullPointerException e) {
            return DataResult.error("Not a valid Recipe Requirement Type: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<CraftingManager.PHASE> decodePhase(String encoded) {
        try {
            return DataResult.success(CraftingManager.PHASE.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Phase: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<WeatherMachineComponent.WeatherType> decodeWeather(String encoded) {
        try {
            return DataResult.success(WeatherMachineComponent.WeatherType.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Weather Type: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<ComparatorMode> decodeComparatorMode(String encoded) {
        try {
            return DataResult.success(ComparatorMode.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Comparator Mode: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<CompoundNBT> decodeCompoundNBT(String encoded) {
        try {
            return DataResult.success(JsonToNBT.getTagFromJson(encoded));
        } catch (CommandSyntaxException e) {
            return DataResult.error("Not a valid NBT: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<EntityRequirement.ACTION> decodeEntityRequirementAction(String encoded) {
        try {
            return DataResult.success(EntityRequirement.ACTION.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Entity Requirement Mode: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<BlockRequirement.ACTION> decodeBlockRequirementAction(String encoded) {
        try {
            return DataResult.success(BlockRequirement.ACTION.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid Block Requirement Mode: " + encoded + " " + e.getMessage());
        }
    }

    private static DataResult<RecipeModifier.OPERATION> decodeModifierOperation(String encoded) {
        try {
            return DataResult.success(RecipeModifier.OPERATION.value(encoded));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Not a valid modifier operation: " + encoded + " " + e.getMessage());
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
