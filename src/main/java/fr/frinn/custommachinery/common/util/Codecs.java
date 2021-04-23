package fr.frinn.custommachinery.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.crafting.requirements.RequirementType;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.MachineLocation;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.gui.GuiElementType;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import fr.frinn.custommachinery.common.data.gui.TextGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

import java.util.Objects;

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

    public static final Codec<GuiElementType<? extends IGuiElement>> GUI_ELEMENT_TYPE_CODEC                   = ResourceLocation.CODEC.comapFlatMap(Codecs::decodeGuiElementType, GuiElementType::getRegistryName);
    public static final Codec<MachineComponentType<? extends IMachineComponent>> MACHINE_COMPONENT_TYPE_CODEC = ResourceLocation.CODEC.comapFlatMap(Codecs::decodeMachineComponentType, MachineComponentType::getRegistryName);
    public static final Codec<RequirementType<? extends IRequirement>> REQUIREMENT_TYPE_CODEC                                      = ResourceLocation.CODEC.comapFlatMap(Codecs::decodeRecipeRequirementType, RequirementType::getRegistryName);

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
}
