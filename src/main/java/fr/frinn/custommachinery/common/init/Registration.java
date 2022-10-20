package fr.frinn.custommachinery.common.init;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.render.element.jei.EnergyJEIIngredientRenderer;
import fr.frinn.custommachinery.client.render.element.jei.FluidStackIngredientRenderer;
import fr.frinn.custommachinery.client.render.element.jei.ItemStackJEIIngredientRenderer;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipeSerializer;
import fr.frinn.custommachinery.common.crafting.requirement.BiomeRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.BlockRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.CommandRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.DimensionRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.DropRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.DurabilityRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.EffectRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.EnergyPerTickRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.EnergyRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.EntityRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.FluidPerTickRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.FluidRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.FuelRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.FunctionRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.ItemRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.LightRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.LootTableRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.PositionRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.RedstoneRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.SpeedRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.StructureRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.TimeRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.WeatherRequirement;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.builder.component.EnergyComponentBuilder;
import fr.frinn.custommachinery.common.data.builder.component.FluidComponentBuilder;
import fr.frinn.custommachinery.common.data.builder.component.ItemComponentBuilder;
import fr.frinn.custommachinery.common.data.component.BlockMachineComponent;
import fr.frinn.custommachinery.common.data.component.CommandMachineComponent;
import fr.frinn.custommachinery.common.data.component.DropMachineComponent;
import fr.frinn.custommachinery.common.data.component.EffectMachineComponent;
import fr.frinn.custommachinery.common.data.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.data.component.EntityMachineComponent;
import fr.frinn.custommachinery.common.data.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.data.component.FuelMachineComponent;
import fr.frinn.custommachinery.common.data.component.FunctionMachineComponent;
import fr.frinn.custommachinery.common.data.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.data.component.LightMachineComponent;
import fr.frinn.custommachinery.common.data.component.PositionMachineComponent;
import fr.frinn.custommachinery.common.data.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.data.component.StructureMachineComponent;
import fr.frinn.custommachinery.common.data.component.TimeMachineComponent;
import fr.frinn.custommachinery.common.data.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.data.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.data.component.variant.item.DefaultItemComponentVariant;
import fr.frinn.custommachinery.common.data.component.variant.item.FuelItemComponentVariant;
import fr.frinn.custommachinery.common.data.component.variant.item.UpgradeItemComponentVariant;
import fr.frinn.custommachinery.common.data.gui.DumpGuiElement;
import fr.frinn.custommachinery.common.data.gui.EnergyGuiElement;
import fr.frinn.custommachinery.common.data.gui.FluidGuiElement;
import fr.frinn.custommachinery.common.data.gui.FuelGuiElement;
import fr.frinn.custommachinery.common.data.gui.PlayerInventoryGuiElement;
import fr.frinn.custommachinery.common.data.gui.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.data.gui.ResetGuiElement;
import fr.frinn.custommachinery.common.data.gui.SizeGuiElement;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import fr.frinn.custommachinery.common.data.gui.StatusGuiElement;
import fr.frinn.custommachinery.common.data.gui.TextGuiElement;
import fr.frinn.custommachinery.common.data.gui.TextureGuiElement;
import fr.frinn.custommachinery.common.util.Codecs;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class Registration {

    @MethodsReturnNonnullByDefault
    public static final ItemGroup GROUP = new ItemGroup(CustomMachinery.MODID) {
        @Override
        public ItemStack createIcon() {
            return CustomMachineItem.makeMachineItem(CustomMachine.DUMMY.getId());
        }

        @ParametersAreNonnullByDefault
        @Override
        public void fill(NonNullList<ItemStack> items) {
            ITEMS.getEntries().forEach(item -> item.get().fillItemGroup(this, items));
        }
    };

    public static final IRecipeType<CustomMachineRecipe> CUSTOM_MACHINE_RECIPE = new IRecipeType<CustomMachineRecipe>(){};

    public static final LootParameterSet CUSTOM_MACHINE_LOOT_PARAMETER_SET = LootParameterSets.register("custom_machine", builder ->
            builder.optional(LootParameters.ORIGIN).optional(LootParameters.BLOCK_ENTITY)
    );

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CustomMachinery.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CustomMachinery.MODID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, CustomMachinery.MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, CustomMachinery.MODID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CustomMachinery.MODID);
    public static final DeferredRegister<GuiElementType<? extends IGuiElement>> GUI_ELEMENTS = DeferredRegister.create((Class)GuiElementType.class, CustomMachinery.MODID);
    public static final DeferredRegister<MachineComponentType<? extends IMachineComponent>> MACHINE_COMPONENTS = DeferredRegister.create((Class)MachineComponentType.class, CustomMachinery.MODID);
    public static final DeferredRegister<RequirementType<? extends IRequirement<?>>> REQUIREMENTS = DeferredRegister.create((Class)RequirementType.class, CustomMachinery.MODID);
    public static final DeferredRegister<MachineAppearanceProperty<?>> APPEARANCE_PROPERTIES = DeferredRegister.create((Class)MachineAppearanceProperty.class, CustomMachinery.MODID);

    public static final Supplier<IForgeRegistry<GuiElementType<? extends IGuiElement>>> GUI_ELEMENT_TYPE_REGISTRY = GUI_ELEMENTS.makeRegistry("gui_element_type", RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<MachineComponentType<? extends IMachineComponent>>> MACHINE_COMPONENT_TYPE_REGISTRY = MACHINE_COMPONENTS.makeRegistry("component_type", RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<RequirementType<? extends IRequirement<?>>>> REQUIREMENT_TYPE_REGISTRY = REQUIREMENTS.makeRegistry("requirement_type", RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<MachineAppearanceProperty<?>>> APPEARANCE_PROPERTY_REGISTRY = APPEARANCE_PROPERTIES.makeRegistry("appearance_property", RegistryBuilder::new);

    public static final RegistryObject<CustomMachineBlock> CUSTOM_MACHINE_BLOCK = BLOCKS.register("custom_machine_block", CustomMachineBlock::new);

    public static final RegistryObject<CustomMachineItem> CUSTOM_MACHINE_ITEM = ITEMS.register("custom_machine_item", () -> new CustomMachineItem(CUSTOM_MACHINE_BLOCK.get(), new Item.Properties().group(GROUP)));
    public static final RegistryObject<MachineCreatorItem> MACHINE_CREATOR_ITEM = ITEMS.register("machine_creator_item", () ->  new MachineCreatorItem(new Item.Properties().group(GROUP).maxStackSize(1)));
    public static final RegistryObject<BoxCreatorItem> BOX_CREATOR_ITEM = ITEMS.register("box_creator_item", () -> new BoxCreatorItem(new Item.Properties().group(GROUP).maxStackSize(1)));
    public static final RegistryObject<StructureCreatorItem> STRUCTURE_CREATOR_ITEM = ITEMS.register("structure_creator", () -> new StructureCreatorItem(new Item.Properties().group(GROUP).maxStackSize(1)));

    public static final RegistryObject<TileEntityType<CustomMachineTile>> CUSTOM_MACHINE_TILE = TILE_ENTITIES.register("custom_machine_tile", () -> TileEntityType.Builder.create(CustomMachineTile::new, CUSTOM_MACHINE_BLOCK.get()).build(null));

    public static final RegistryObject<ContainerType<CustomMachineContainer>> CUSTOM_MACHINE_CONTAINER = CONTAINERS.register("custom_machine_container", () -> IForgeContainerType.create(CustomMachineContainer::new));

    public static final RegistryObject<CustomMachineRecipeSerializer> CUSTOM_MACHINE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("custom_machine", CustomMachineRecipeSerializer::new);

    public static final RegistryObject<GuiElementType<EnergyGuiElement>> ENERGY_GUI_ELEMENT = GUI_ELEMENTS.register("energy", () -> new GuiElementType<>(EnergyGuiElement.CODEC).setJeiIngredientType(() -> EnergyJEIIngredientRenderer::new));
    public static final RegistryObject<GuiElementType<FluidGuiElement>> FLUID_GUI_ELEMENT = GUI_ELEMENTS.register("fluid", () -> new GuiElementType<>(FluidGuiElement.CODEC).setJeiIngredientType(() -> FluidStackIngredientRenderer::new));
    public static final RegistryObject<GuiElementType<PlayerInventoryGuiElement>> PLAYER_INVENTORY_GUI_ELEMENT = GUI_ELEMENTS.register("player_inventory", () -> new GuiElementType<>(PlayerInventoryGuiElement.CODEC));
    public static final RegistryObject<GuiElementType<ProgressBarGuiElement>> PROGRESS_GUI_ELEMENT = GUI_ELEMENTS.register("progress", () -> new GuiElementType<>(ProgressBarGuiElement.CODEC));
    public static final RegistryObject<GuiElementType<SlotGuiElement>> SLOT_GUI_ELEMENT = GUI_ELEMENTS.register("slot", () -> new GuiElementType<>(SlotGuiElement.CODEC).setJeiIngredientType(() -> ItemStackJEIIngredientRenderer::new));
    public static final RegistryObject<GuiElementType<StatusGuiElement>> STATUS_GUI_ELEMENT = GUI_ELEMENTS.register("status", () -> new GuiElementType<>(StatusGuiElement.CODEC));
    public static final RegistryObject<GuiElementType<TextureGuiElement>> TEXTURE_GUI_ELEMENT = GUI_ELEMENTS.register("texture", () -> new GuiElementType<>(TextureGuiElement.CODEC));
    public static final RegistryObject<GuiElementType<TextGuiElement>> TEXT_GUI_ELEMENT = GUI_ELEMENTS.register("text", () -> new GuiElementType<>(TextGuiElement.CODEC));
    public static final RegistryObject<GuiElementType<FuelGuiElement>> FUEL_GUI_ELEMENT = GUI_ELEMENTS.register("fuel", () -> new GuiElementType<>(FuelGuiElement.CODEC));
    public static final RegistryObject<GuiElementType<ResetGuiElement>> RESET_GUI_ELEMENT = GUI_ELEMENTS.register("reset", () -> new GuiElementType<>(ResetGuiElement.CODEC));
    public static final RegistryObject<GuiElementType<DumpGuiElement>> DUMP_GUI_ELEMENT = GUI_ELEMENTS.register("dump", () -> new GuiElementType<>(DumpGuiElement.CODEC));
    public static final RegistryObject<GuiElementType<SizeGuiElement>> SIZE_GUI_ELEMENT = GUI_ELEMENTS.register("size", () -> new GuiElementType<>(SizeGuiElement.CODEC));

    public static final RegistryObject<MachineComponentType<EnergyMachineComponent>> ENERGY_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("energy", () -> new MachineComponentType<>(EnergyMachineComponent.Template.CODEC).setGUIBuilder(EnergyComponentBuilder::new));
    public static final RegistryObject<MachineComponentType<FluidMachineComponent>> FLUID_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("fluid", () -> new MachineComponentType<>(FluidMachineComponent.Template.CODEC).setNotSingle(FluidComponentHandler::new).setGUIBuilder(FluidComponentBuilder::new));
    public static final RegistryObject<MachineComponentType<ItemMachineComponent>> ITEM_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("item", () -> new MachineComponentType<>(ItemMachineComponent.Template.CODEC).setNotSingle(ItemComponentHandler::new).setGUIBuilder(ItemComponentBuilder::new));
    public static final RegistryObject<MachineComponentType<PositionMachineComponent>> POSITION_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("position", () -> new MachineComponentType<>(PositionMachineComponent::new));
    public static final RegistryObject<MachineComponentType<TimeMachineComponent>> TIME_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("time", () -> new MachineComponentType<>(TimeMachineComponent::new));
    public static final RegistryObject<MachineComponentType<CommandMachineComponent>> COMMAND_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("command", () -> new MachineComponentType<>(CommandMachineComponent::new));
    public static final RegistryObject<MachineComponentType<FuelMachineComponent>> FUEL_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("fuel", () -> new MachineComponentType<>(FuelMachineComponent::new));
    public static final RegistryObject<MachineComponentType<EffectMachineComponent>> EFFECT_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("effect", () -> new MachineComponentType<>(EffectMachineComponent::new));
    public static final RegistryObject<MachineComponentType<WeatherMachineComponent>> WEATHER_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("weather", () -> new MachineComponentType<>(WeatherMachineComponent::new));
    public static final RegistryObject<MachineComponentType<RedstoneMachineComponent>> REDSTONE_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("redstone", () -> new MachineComponentType<>(RedstoneMachineComponent.Template.CODEC, RedstoneMachineComponent::new));
    public static final RegistryObject<MachineComponentType<EntityMachineComponent>> ENTITY_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("entity", () -> new MachineComponentType<>(EntityMachineComponent::new));
    public static final RegistryObject<MachineComponentType<LightMachineComponent>> LIGHT_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("light", () -> new MachineComponentType<>(LightMachineComponent::new));
    public static final RegistryObject<MachineComponentType<BlockMachineComponent>> BLOCK_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("block", () -> new MachineComponentType<>(BlockMachineComponent::new));
    public static final RegistryObject<MachineComponentType<StructureMachineComponent>> STRUCTURE_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("structure", () -> new MachineComponentType<>(StructureMachineComponent::new));
    public static final RegistryObject<MachineComponentType<DropMachineComponent>> DROP_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("drop", () -> new MachineComponentType<>(DropMachineComponent::new));
    public static final RegistryObject<MachineComponentType<FunctionMachineComponent>> FUNCTION_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("function", () -> new MachineComponentType<>(FunctionMachineComponent::new));

    public static final RegistryObject<RequirementType<ItemRequirement>> ITEM_REQUIREMENT = REQUIREMENTS.register("item", () -> new RequirementType<>(ItemRequirement.CODEC));
    public static final RegistryObject<RequirementType<EnergyRequirement>> ENERGY_REQUIREMENT = REQUIREMENTS.register("energy", () -> new RequirementType<>(EnergyRequirement.CODEC));
    public static final RegistryObject<RequirementType<EnergyPerTickRequirement>> ENERGY_PER_TICK_REQUIREMENT = REQUIREMENTS.register("energy_per_tick", () -> new RequirementType<>(EnergyPerTickRequirement.CODEC));
    public static final RegistryObject<RequirementType<FluidRequirement>> FLUID_REQUIREMENT = REQUIREMENTS.register("fluid", () -> new RequirementType<>(FluidRequirement.CODEC));
    public static final RegistryObject<RequirementType<FluidPerTickRequirement>> FLUID_PER_TICK_REQUIREMENT = REQUIREMENTS.register("fluid_per_tick", () -> new RequirementType<>(FluidPerTickRequirement.CODEC));
    public static final RegistryObject<RequirementType<PositionRequirement>> POSITION_REQUIREMENT = REQUIREMENTS.register("position", () -> new RequirementType<>(PositionRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<TimeRequirement>> TIME_REQUIREMENT = REQUIREMENTS.register("time", () -> new RequirementType<>(TimeRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<CommandRequirement>> COMMAND_REQUIREMENT = REQUIREMENTS.register("command", () -> new RequirementType<>(CommandRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<FuelRequirement>> FUEL_REQUIREMENT = REQUIREMENTS.register("fuel", () -> new RequirementType<>(FuelRequirement.CODEC));
    public static final RegistryObject<RequirementType<EffectRequirement>> EFFECT_REQUIREMENT = REQUIREMENTS.register("effect", () -> new RequirementType<>(EffectRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<WeatherRequirement>> WEATHER_REQUIREMENT = REQUIREMENTS.register("weather", () -> new RequirementType<>(WeatherRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<RedstoneRequirement>> REDSTONE_REQUIREMENT = REQUIREMENTS.register("redstone", () -> new RequirementType<>(RedstoneRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<EntityRequirement>> ENTITY_REQUIREMENT = REQUIREMENTS.register("entity", () -> new RequirementType<>(EntityRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<LightRequirement>> LIGHT_REQUIREMENT = REQUIREMENTS.register("light", () -> new RequirementType<>(LightRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<BlockRequirement>> BLOCK_REQUIREMENT = REQUIREMENTS.register("block", () -> new RequirementType<>(BlockRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<SpeedRequirement>> SPEED_REQUIREMENT = REQUIREMENTS.register("speed", () -> new RequirementType<>(SpeedRequirement.CODEC));
    public static final RegistryObject<RequirementType<DurabilityRequirement>> DURABILITY_REQUIREMENT = REQUIREMENTS.register("durability", () -> new RequirementType<>(DurabilityRequirement.CODEC));
    public static final RegistryObject<RequirementType<StructureRequirement>> STRUCTURE_REQUIREMENT = REQUIREMENTS.register("structure", () -> new RequirementType<>(StructureRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<LootTableRequirement>> LOOT_TABLE_REQUIREMENT = REQUIREMENTS.register("loot_table", () -> new RequirementType<>(LootTableRequirement.CODEC));
    public static final RegistryObject<RequirementType<BiomeRequirement>> BIOME_REQUIREMENT = REQUIREMENTS.register("biome", () -> new RequirementType<>(BiomeRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<DimensionRequirement>> DIMENSION_REQUIREMENT = REQUIREMENTS.register("dimension", () -> new RequirementType<>(DimensionRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<DropRequirement>> DROP_REQUIREMENT = REQUIREMENTS.register("drop", () -> new RequirementType<>(DropRequirement.CODEC).setWorldRequirement());
    public static final RegistryObject<RequirementType<FunctionRequirement>> FUNCTION_REQUIREMENT = REQUIREMENTS.register("function", () -> new RequirementType<>(FunctionRequirement.CODEC).setWorldRequirement());

    public static final RegistryObject<MachineAppearanceProperty<ResourceLocation>> BLOCK_MODEL_PROPERTY = APPEARANCE_PROPERTIES.register("block", () -> new MachineAppearanceProperty<>(Codecs.BLOCK_MODEL_CODEC, new ResourceLocation(CustomMachinery.MODID, "block/custom_machine_block")));
    public static final RegistryObject<MachineAppearanceProperty<ResourceLocation>> ITEM_MODEL_PROPERTY = APPEARANCE_PROPERTIES.register("item", () -> new MachineAppearanceProperty<>(Codecs.ITEM_MODEL_CODEC, new ResourceLocation(CustomMachinery.MODID, "block/custom_machine_block")));
    public static final RegistryObject<MachineAppearanceProperty<SoundEvent>> SOUND_PROPERTY = APPEARANCE_PROPERTIES.register("sound", () -> new MachineAppearanceProperty<>(SoundEvent.CODEC, new SoundEvent(new ResourceLocation(""))));
    public static final RegistryObject<MachineAppearanceProperty<Integer>> LIGHT_PROPERTY = APPEARANCE_PROPERTIES.register("light", () -> new MachineAppearanceProperty<>(Codec.intRange(0, 15), 0));
    public static final RegistryObject<MachineAppearanceProperty<Integer>> COLOR_PROPERTY = APPEARANCE_PROPERTIES.register("color", () -> new MachineAppearanceProperty<>(Codec.INT, 0xFFFFFF));
    public static final RegistryObject<MachineAppearanceProperty<Float>> HARDNESS_PROPERTY = APPEARANCE_PROPERTIES.register("hardness", () -> new MachineAppearanceProperty<>(Codec.floatRange(0, Float.MAX_VALUE), 3.5F));
    public static final RegistryObject<MachineAppearanceProperty<Float>> RESISTANCE_PROPERTY = APPEARANCE_PROPERTIES.register("resistance", () -> new MachineAppearanceProperty<>(Codec.floatRange(0, Float.MAX_VALUE), 3.5F));
    public static final RegistryObject<MachineAppearanceProperty<ToolType>> TOOL_TYPE_PROPERTY = APPEARANCE_PROPERTIES.register("tool_type", () -> new MachineAppearanceProperty<>(Codecs.TOOL_TYPE_CODEC, ToolType.PICKAXE));
    public static final RegistryObject<MachineAppearanceProperty<Integer>> MINING_LEVEL_PROPERTY = APPEARANCE_PROPERTIES.register("mining_level", () -> new MachineAppearanceProperty<>(Codec.INT, 1));
    public static final RegistryObject<MachineAppearanceProperty<VoxelShape>> SHAPE_PROPERTY = APPEARANCE_PROPERTIES.register("shape", () -> new MachineAppearanceProperty<>(Codecs.VOXEL_SHAPE_CODEC, VoxelShapes.fullCube()));

    public static void registerRecipeType(final RegistryEvent<IRecipeSerializer<?>> event) {
        Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(CustomMachinery.MODID, "custom_machine"), CUSTOM_MACHINE_RECIPE);
    }

    public static void registerComponentVariants() {
        ITEM_MACHINE_COMPONENT.get().addVariant(DefaultItemComponentVariant.INSTANCE);
        ITEM_MACHINE_COMPONENT.get().addVariant(FuelItemComponentVariant.INSTANCE);
        ITEM_MACHINE_COMPONENT.get().addVariant(UpgradeItemComponentVariant.INSTANCE);
    }
}
