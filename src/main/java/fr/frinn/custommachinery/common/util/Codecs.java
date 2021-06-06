package fr.frinn.custommachinery.common.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.requirements.BlockRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.EntityRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.RequirementType;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.MachineLocation;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.data.gui.GuiElementType;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import fr.frinn.custommachinery.common.data.gui.TextGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Codecs {

    public static final Codec<ModelResourceLocation> MODEL_RESOURCE_LOCATION_CODEC      = Codec.STRING.comapFlatMap(Codecs::decodeModelResourceLocation, ModelResourceLocation::toString).stable();
    public static final Codec<MachineAppearance.AppearanceType> APPEARANCE_TYPE_CODEC   = Codec.STRING.comapFlatMap(Codecs::decodeAppearanceType, MachineAppearance.AppearanceType::toString).stable();
    public static final Codec<MachineAppearance.LightMode> LIGHT_MODE_CODEC             = Codec.STRING.comapFlatMap(Codecs::decodeLightMode, MachineAppearance.LightMode::toString).stable();
    public static final Codec<IMachineComponent.Mode> COMPONENT_MODE_CODEC              = Codec.STRING.comapFlatMap(Codecs::decodeMachineComponentMode, IMachineComponent.Mode::toString).stable();
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

    public static final Codec<GuiElementType<? extends IGuiElement>> GUI_ELEMENT_TYPE_CODEC                   = ResourceLocation.CODEC.comapFlatMap(Codecs::decodeGuiElementType, GuiElementType::getRegistryName).stable();
    public static final Codec<MachineComponentType<? extends IMachineComponent>> MACHINE_COMPONENT_TYPE_CODEC = ResourceLocation.CODEC.comapFlatMap(Codecs::decodeMachineComponentType, MachineComponentType::getRegistryName).stable();
    public static final Codec<RequirementType<? extends IRequirement>> REQUIREMENT_TYPE_CODEC                 = ResourceLocation.CODEC.comapFlatMap(Codecs::decodeRecipeRequirementType, RequirementType::getRegistryName).stable();

    public static final Codec<AxisAlignedBB> BOX_CODEC = Codec.INT_STREAM.comapFlatMap(stream -> Util.validateIntStreamSize(stream, 6).map(array -> new AxisAlignedBB(array[0], array[1], array[2], array[3], array[4], array[5])), box -> IntStream.of((int)box.minX, (int)box.minY, (int)box.minZ, (int)box.maxX, (int)box.maxY, (int)box.maxZ));
    public static final Codec<PartialBlockState> BLOCK_STATE_CODEC = MODEL_RESOURCE_LOCATION_CODEC.comapFlatMap(Codecs::decodeBlockState, PartialBlockState::location).stable();

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

    private static DataResult<IMachineComponent.Mode> decodeMachineComponentMode(String encoded) {
        try {
            return DataResult.success(IMachineComponent.Mode.value(encoded));
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

    private static DataResult<PartialBlockState> decodeBlockState(ModelResourceLocation encoded) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(encoded.getNamespace(), encoded.getPath()));
        if(block == null)
            return DataResult.error("Not a valid block: '" + encoded + "'");
        PartialBlockState state = new PartialBlockState(block);
        if(encoded.getVariant().isEmpty())
            return DataResult.success(state);
        for(String property : encoded.getVariant().split(",")) {
            String[] keyValue = property.split("=");
            if(keyValue.length != 2)
                return DataResult.error("Bad formating in: '" + property + "' for block: '" + encoded + "'. Except: 'key=value'");
            String key = keyValue[0];
            String value = keyValue[1];
            Property stateProperty = block.getStateContainer().getProperty(key);
            if(stateProperty == null)
                return DataResult.error("Unknown BlockState property: '" + key + "' in: '" + encoded + "'");
            Optional<Comparable> optionalValue = stateProperty.parseValue(value);
            if(!optionalValue.isPresent())
                return DataResult.error("Unknown BlockState property value: '" + value + "' for property: '" + key + "' in: '" + encoded + "'");
            state = state.with(stateProperty, optionalValue.get());
        }
        return DataResult.success(state);
    }
}
