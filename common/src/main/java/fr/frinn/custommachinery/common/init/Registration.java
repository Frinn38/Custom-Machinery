package fr.frinn.custommachinery.common.init;

import com.mojang.serialization.Codec;
import dev.architectury.fluid.FluidStack;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.PlatformHelper;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.BlockMachineComponent;
import fr.frinn.custommachinery.common.component.CommandMachineComponent;
import fr.frinn.custommachinery.common.component.DropMachineComponent;
import fr.frinn.custommachinery.common.component.EffectMachineComponent;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.EntityMachineComponent;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.FuelMachineComponent;
import fr.frinn.custommachinery.common.component.FunctionMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.LightMachineComponent;
import fr.frinn.custommachinery.common.component.PositionMachineComponent;
import fr.frinn.custommachinery.common.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.component.StructureMachineComponent;
import fr.frinn.custommachinery.common.component.TimeMachineComponent;
import fr.frinn.custommachinery.common.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.component.variant.item.DefaultItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.FuelItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.ResultItemComponentVariant;
import fr.frinn.custommachinery.common.component.variant.item.UpgradeItemComponentVariant;
import fr.frinn.custommachinery.common.crafting.DummyProcessor;
import fr.frinn.custommachinery.common.crafting.craft.CraftProcessor;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipeSerializer;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipeSerializer;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import fr.frinn.custommachinery.common.guielement.DumpGuiElement;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.guielement.FuelGuiElement;
import fr.frinn.custommachinery.common.guielement.PlayerInventoryGuiElement;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.guielement.ResetGuiElement;
import fr.frinn.custommachinery.common.guielement.SizeGuiElement;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.guielement.StatusGuiElement;
import fr.frinn.custommachinery.common.guielement.TextGuiElement;
import fr.frinn.custommachinery.common.guielement.TextureGuiElement;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.builder.component.EnergyComponentBuilder;
import fr.frinn.custommachinery.common.machine.builder.component.FluidComponentBuilder;
import fr.frinn.custommachinery.common.machine.builder.component.ItemComponentBuilder;
import fr.frinn.custommachinery.common.network.data.BooleanData;
import fr.frinn.custommachinery.common.network.data.DoubleData;
import fr.frinn.custommachinery.common.network.data.FluidStackData;
import fr.frinn.custommachinery.common.network.data.IntegerData;
import fr.frinn.custommachinery.common.network.data.ItemStackData;
import fr.frinn.custommachinery.common.network.data.LongData;
import fr.frinn.custommachinery.common.network.data.SideConfigData;
import fr.frinn.custommachinery.common.network.data.StringData;
import fr.frinn.custommachinery.common.network.syncable.BooleanSyncable;
import fr.frinn.custommachinery.common.network.syncable.DoubleSyncable;
import fr.frinn.custommachinery.common.network.syncable.FluidStackSyncable;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.common.network.syncable.ItemStackSyncable;
import fr.frinn.custommachinery.common.network.syncable.LongSyncable;
import fr.frinn.custommachinery.common.network.syncable.SideConfigSyncable;
import fr.frinn.custommachinery.common.network.syncable.StringSyncable;
import fr.frinn.custommachinery.common.requirement.BiomeRequirement;
import fr.frinn.custommachinery.common.requirement.BlockRequirement;
import fr.frinn.custommachinery.common.requirement.CommandRequirement;
import fr.frinn.custommachinery.common.requirement.DimensionRequirement;
import fr.frinn.custommachinery.common.requirement.DropRequirement;
import fr.frinn.custommachinery.common.requirement.DurabilityRequirement;
import fr.frinn.custommachinery.common.requirement.EffectRequirement;
import fr.frinn.custommachinery.common.requirement.EnergyPerTickRequirement;
import fr.frinn.custommachinery.common.requirement.EnergyRequirement;
import fr.frinn.custommachinery.common.requirement.EntityRequirement;
import fr.frinn.custommachinery.common.requirement.FluidPerTickRequirement;
import fr.frinn.custommachinery.common.requirement.FluidRequirement;
import fr.frinn.custommachinery.common.requirement.FuelRequirement;
import fr.frinn.custommachinery.common.requirement.FunctionRequirement;
import fr.frinn.custommachinery.common.requirement.ItemRequirement;
import fr.frinn.custommachinery.common.requirement.ItemTransformRequirement;
import fr.frinn.custommachinery.common.requirement.LightRequirement;
import fr.frinn.custommachinery.common.requirement.LootTableRequirement;
import fr.frinn.custommachinery.common.requirement.PositionRequirement;
import fr.frinn.custommachinery.common.requirement.RedstoneRequirement;
import fr.frinn.custommachinery.common.requirement.SpeedRequirement;
import fr.frinn.custommachinery.common.requirement.StructureRequirement;
import fr.frinn.custommachinery.common.requirement.TimeRequirement;
import fr.frinn.custommachinery.common.requirement.WeatherRequirement;
import fr.frinn.custommachinery.common.util.CMSoundType;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import fr.frinn.custommachinery.impl.util.ModelLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class Registration {

    public static final Registries REGISTRIES = Registries.get(CustomMachinery.MODID);

    public static final CreativeModeTab GROUP = CreativeTabRegistry.create(new ResourceLocation(CustomMachinery.MODID, "group"), () -> CustomMachineItem.makeMachineItem(CustomMachine.DUMMY.getId()));

    public static final LootContextParamSet CUSTOM_MACHINE_LOOT_PARAMETER_SET = LootContextParamSets.register("custom_machine", builder ->
            builder.optional(LootContextParams.ORIGIN).optional(LootContextParams.BLOCK_ENTITY)
    );

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(CustomMachinery.MODID, Registry.BLOCK_REGISTRY);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(CustomMachinery.MODID, Registry.ITEM_REGISTRY);
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(CustomMachinery.MODID, Registry.BLOCK_ENTITY_TYPE_REGISTRY);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(CustomMachinery.MODID, Registry.MENU_REGISTRY);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(CustomMachinery.MODID, Registry.RECIPE_SERIALIZER_REGISTRY);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(CustomMachinery.MODID, Registry.RECIPE_TYPE_REGISTRY);
    public static final DeferredRegister<GuiElementType<? extends IGuiElement>> GUI_ELEMENTS = DeferredRegister.create(CustomMachinery.MODID, GuiElementType.REGISTRY_KEY);
    public static final DeferredRegister<MachineComponentType<? extends IMachineComponent>> MACHINE_COMPONENTS = DeferredRegister.create(CustomMachinery.MODID, MachineComponentType.REGISTRY_KEY);
    public static final DeferredRegister<RequirementType<? extends IRequirement<?>>> REQUIREMENTS = DeferredRegister.create(CustomMachinery.MODID, RequirementType.REGISTRY_KEY);
    public static final DeferredRegister<MachineAppearanceProperty<?>> APPEARANCE_PROPERTIES = DeferredRegister.create(CustomMachinery.MODID, MachineAppearanceProperty.REGISTRY_KEY);
    public static final DeferredRegister<DataType<?, ?>> DATAS = DeferredRegister.create(CustomMachinery.MODID, DataType.REGISTRY_KEY);
    public static final DeferredRegister<ProcessorType<?>> PROCESSORS = DeferredRegister.create(CustomMachinery.MODID, ProcessorType.REGISTRY_KEY);

    public static final Registrar<GuiElementType<? extends IGuiElement>> GUI_ELEMENT_TYPE_REGISTRY = REGISTRIES.builder(GuiElementType.REGISTRY_KEY.location(), new GuiElementType<?>[]{}).build();
    public static final Registrar<MachineComponentType<? extends IMachineComponent>> MACHINE_COMPONENT_TYPE_REGISTRY = REGISTRIES.builder(MachineComponentType.REGISTRY_KEY.location(), new MachineComponentType<?>[]{}).build();
    public static final Registrar<RequirementType<? extends IRequirement<?>>> REQUIREMENT_TYPE_REGISTRY = REGISTRIES.builder(RequirementType.REGISTRY_KEY.location(), new RequirementType<?>[]{}).build();
    public static final Registrar<MachineAppearanceProperty<?>> APPEARANCE_PROPERTY_REGISTRY = REGISTRIES.builder(MachineAppearanceProperty.REGISTRY_KEY.location(), new MachineAppearanceProperty<?>[]{}).build();
    public static final Registrar<DataType<?, ?>> DATA_REGISTRY = REGISTRIES.builder(DataType.REGISTRY_KEY.location(), new DataType<?, ?>[]{}).build();
    public static final Registrar<ProcessorType<?>> PROCESSOR_REGISTRY = REGISTRIES.builder(ProcessorType.REGISTRY_KEY.location(), new ProcessorType<?>[]{}).build();

    public static final RegistrySupplier<CustomMachineBlock> CUSTOM_MACHINE_BLOCK = BLOCKS.register("custom_machine_block", PlatformHelper::createMachineBlock);

    public static final RegistrySupplier<CustomMachineItem> CUSTOM_MACHINE_ITEM = ITEMS.register("custom_machine_item", () -> new CustomMachineItem(CUSTOM_MACHINE_BLOCK.get(), new Item.Properties().tab(GROUP)));
    public static final RegistrySupplier<MachineCreatorItem> MACHINE_CREATOR_ITEM = ITEMS.register("machine_creator_item", () ->  new MachineCreatorItem(new Item.Properties().tab(GROUP).stacksTo(1)));
    public static final RegistrySupplier<BoxCreatorItem> BOX_CREATOR_ITEM = ITEMS.register("box_creator_item", () -> new BoxCreatorItem(new Item.Properties().tab(GROUP).stacksTo(1)));
    public static final RegistrySupplier<StructureCreatorItem> STRUCTURE_CREATOR_ITEM = ITEMS.register("structure_creator", () -> new StructureCreatorItem(new Item.Properties().tab(GROUP).stacksTo(1)));

    public static final RegistrySupplier<BlockEntityType<CustomMachineTile>> CUSTOM_MACHINE_TILE = TILE_ENTITIES.register("custom_machine_tile", () -> BlockEntityType.Builder.of(PlatformHelper::createMachineTile, CUSTOM_MACHINE_BLOCK.get()).build(null));

    public static final RegistrySupplier<MenuType<CustomMachineContainer>> CUSTOM_MACHINE_CONTAINER = CONTAINERS.register("custom_machine_container", () -> MenuRegistry.ofExtended(CustomMachineContainer::new));

    public static final RegistrySupplier<CustomMachineRecipeSerializer> CUSTOM_MACHINE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("custom_machine", CustomMachineRecipeSerializer::new);
    public static final RegistrySupplier<CustomCraftRecipeSerializer> CUSTOM_CRAFT_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("custom_craft", CustomCraftRecipeSerializer::new);

    public static final RegistrySupplier<RecipeType<CustomMachineRecipe>> CUSTOM_MACHINE_RECIPE = RECIPE_TYPES.register("custom_machine", () -> new RecipeType<>() {});
    public static final RegistrySupplier<RecipeType<CustomCraftRecipe>> CUSTOM_CRAFT_RECIPE = RECIPE_TYPES.register("custom_craft", () -> new RecipeType<>() {});

    public static final RegistrySupplier<GuiElementType<EnergyGuiElement>> ENERGY_GUI_ELEMENT = GUI_ELEMENTS.register("energy", () -> new GuiElementType<>(EnergyGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<FluidGuiElement>> FLUID_GUI_ELEMENT = GUI_ELEMENTS.register("fluid", () -> new GuiElementType<>(FluidGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<PlayerInventoryGuiElement>> PLAYER_INVENTORY_GUI_ELEMENT = GUI_ELEMENTS.register("player_inventory", () -> new GuiElementType<>(PlayerInventoryGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<ProgressBarGuiElement>> PROGRESS_GUI_ELEMENT = GUI_ELEMENTS.register("progress", () -> new GuiElementType<>(ProgressBarGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<SlotGuiElement>> SLOT_GUI_ELEMENT = GUI_ELEMENTS.register("slot", () -> new GuiElementType<>(SlotGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<StatusGuiElement>> STATUS_GUI_ELEMENT = GUI_ELEMENTS.register("status", () -> new GuiElementType<>(StatusGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<TextureGuiElement>> TEXTURE_GUI_ELEMENT = GUI_ELEMENTS.register("texture", () -> new GuiElementType<>(TextureGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<TextGuiElement>> TEXT_GUI_ELEMENT = GUI_ELEMENTS.register("text", () -> new GuiElementType<>(TextGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<FuelGuiElement>> FUEL_GUI_ELEMENT = GUI_ELEMENTS.register("fuel", () -> new GuiElementType<>(FuelGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<ResetGuiElement>> RESET_GUI_ELEMENT = GUI_ELEMENTS.register("reset", () -> new GuiElementType<>(ResetGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<DumpGuiElement>> DUMP_GUI_ELEMENT = GUI_ELEMENTS.register("dump", () -> new GuiElementType<>(DumpGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<SizeGuiElement>> SIZE_GUI_ELEMENT = GUI_ELEMENTS.register("size", () -> new GuiElementType<>(SizeGuiElement.CODEC));
    public static final RegistrySupplier<GuiElementType<ConfigGuiElement>> CONFIG_GUI_ELEMENT = GUI_ELEMENTS.register("config", () -> new GuiElementType<>(ConfigGuiElement.CODEC));

    public static final RegistrySupplier<MachineComponentType<EnergyMachineComponent>> ENERGY_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("energy", () -> new MachineComponentType<>(EnergyMachineComponent.Template.CODEC).setGUIBuilder(EnergyComponentBuilder::new));
    public static final RegistrySupplier<MachineComponentType<FluidMachineComponent>> FLUID_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("fluid", () -> new MachineComponentType<>(FluidMachineComponent.Template.CODEC).setNotSingle(FluidComponentHandler::new).setGUIBuilder(FluidComponentBuilder::new));
    public static final RegistrySupplier<MachineComponentType<ItemMachineComponent>> ITEM_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("item", () -> new MachineComponentType<>(ItemMachineComponent.Template.CODEC).setNotSingle(ItemComponentHandler::new).setGUIBuilder(ItemComponentBuilder::new));
    public static final RegistrySupplier<MachineComponentType<PositionMachineComponent>> POSITION_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("position", () -> new MachineComponentType<>(PositionMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<TimeMachineComponent>> TIME_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("time", () -> new MachineComponentType<>(TimeMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<CommandMachineComponent>> COMMAND_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("command", () -> new MachineComponentType<>(CommandMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<FuelMachineComponent>> FUEL_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("fuel", () -> new MachineComponentType<>(FuelMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<EffectMachineComponent>> EFFECT_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("effect", () -> new MachineComponentType<>(EffectMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<WeatherMachineComponent>> WEATHER_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("weather", () -> new MachineComponentType<>(WeatherMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<RedstoneMachineComponent>> REDSTONE_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("redstone", () -> new MachineComponentType<>(RedstoneMachineComponent.Template.CODEC, RedstoneMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<EntityMachineComponent>> ENTITY_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("entity", () -> new MachineComponentType<>(EntityMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<LightMachineComponent>> LIGHT_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("light", () -> new MachineComponentType<>(LightMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<BlockMachineComponent>> BLOCK_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("block", () -> new MachineComponentType<>(BlockMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<StructureMachineComponent>> STRUCTURE_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("structure", () -> new MachineComponentType<>(StructureMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<DropMachineComponent>> DROP_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("drop", () -> new MachineComponentType<>(DropMachineComponent::new));
    public static final RegistrySupplier<MachineComponentType<FunctionMachineComponent>> FUNCTION_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("function", () -> new MachineComponentType<>(FunctionMachineComponent::new));

    public static final RegistrySupplier<RequirementType<ItemRequirement>> ITEM_REQUIREMENT = REQUIREMENTS.register("item", () -> new RequirementType<>(ItemRequirement.CODEC));
    public static final RegistrySupplier<RequirementType<EnergyRequirement>> ENERGY_REQUIREMENT = REQUIREMENTS.register("energy", () -> new RequirementType<>(EnergyRequirement.CODEC));
    public static final RegistrySupplier<RequirementType<EnergyPerTickRequirement>> ENERGY_PER_TICK_REQUIREMENT = REQUIREMENTS.register("energy_per_tick", () -> new RequirementType<>(EnergyPerTickRequirement.CODEC));
    public static final RegistrySupplier<RequirementType<FluidRequirement>> FLUID_REQUIREMENT = REQUIREMENTS.register("fluid", () -> new RequirementType<>(FluidRequirement.CODEC));
    public static final RegistrySupplier<RequirementType<FluidPerTickRequirement>> FLUID_PER_TICK_REQUIREMENT = REQUIREMENTS.register("fluid_per_tick", () -> new RequirementType<>(FluidPerTickRequirement.CODEC));
    public static final RegistrySupplier<RequirementType<PositionRequirement>> POSITION_REQUIREMENT = REQUIREMENTS.register("position", () -> new RequirementType<>(PositionRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<TimeRequirement>> TIME_REQUIREMENT = REQUIREMENTS.register("time", () -> new RequirementType<>(TimeRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<CommandRequirement>> COMMAND_REQUIREMENT = REQUIREMENTS.register("command", () -> new RequirementType<>(CommandRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<FuelRequirement>> FUEL_REQUIREMENT = REQUIREMENTS.register("fuel", () -> new RequirementType<>(FuelRequirement.CODEC));
    public static final RegistrySupplier<RequirementType<EffectRequirement>> EFFECT_REQUIREMENT = REQUIREMENTS.register("effect", () -> new RequirementType<>(EffectRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<WeatherRequirement>> WEATHER_REQUIREMENT = REQUIREMENTS.register("weather", () -> new RequirementType<>(WeatherRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<RedstoneRequirement>> REDSTONE_REQUIREMENT = REQUIREMENTS.register("redstone", () -> new RequirementType<>(RedstoneRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<EntityRequirement>> ENTITY_REQUIREMENT = REQUIREMENTS.register("entity", () -> new RequirementType<>(EntityRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<LightRequirement>> LIGHT_REQUIREMENT = REQUIREMENTS.register("light", () -> new RequirementType<>(LightRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<BlockRequirement>> BLOCK_REQUIREMENT = REQUIREMENTS.register("block", () -> new RequirementType<>(BlockRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<SpeedRequirement>> SPEED_REQUIREMENT = REQUIREMENTS.register("speed", () -> new RequirementType<>(SpeedRequirement.CODEC));
    public static final RegistrySupplier<RequirementType<DurabilityRequirement>> DURABILITY_REQUIREMENT = REQUIREMENTS.register("durability", () -> new RequirementType<>(DurabilityRequirement.CODEC));
    public static final RegistrySupplier<RequirementType<StructureRequirement>> STRUCTURE_REQUIREMENT = REQUIREMENTS.register("structure", () -> new RequirementType<>(StructureRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<LootTableRequirement>> LOOT_TABLE_REQUIREMENT = REQUIREMENTS.register("loot_table", () -> new RequirementType<>(LootTableRequirement.CODEC));
    public static final RegistrySupplier<RequirementType<BiomeRequirement>> BIOME_REQUIREMENT = REQUIREMENTS.register("biome", () -> new RequirementType<>(BiomeRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<DimensionRequirement>> DIMENSION_REQUIREMENT = REQUIREMENTS.register("dimension", () -> new RequirementType<>(DimensionRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<DropRequirement>> DROP_REQUIREMENT = REQUIREMENTS.register("drop", () -> new RequirementType<>(DropRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<FunctionRequirement>> FUNCTION_REQUIREMENT = REQUIREMENTS.register("function", () -> new RequirementType<>(FunctionRequirement.CODEC).setWorldRequirement());
    public static final RegistrySupplier<RequirementType<ItemTransformRequirement>> ITEM_TRANSFORM_REQUIREMENT = REQUIREMENTS.register("item_transform", () -> new RequirementType<>(ItemTransformRequirement.CODEC));

    public static final RegistrySupplier<MachineAppearanceProperty<ModelLocation>> BLOCK_MODEL_PROPERTY = APPEARANCE_PROPERTIES.register("block", () -> new MachineAppearanceProperty<>(Codecs.BLOCK_MODEL_CODEC, ModelLocation.of(new ResourceLocation(CustomMachinery.MODID, "block/custom_machine_block"))));
    public static final RegistrySupplier<MachineAppearanceProperty<ModelLocation>> ITEM_MODEL_PROPERTY = APPEARANCE_PROPERTIES.register("item", () -> new MachineAppearanceProperty<>(Codecs.ITEM_MODEL_CODEC, ModelLocation.of(new ResourceLocation(CustomMachinery.MODID, "block/custom_machine_block"))));
    public static final RegistrySupplier<MachineAppearanceProperty<SoundEvent>> AMBIENT_SOUND_PROPERTY = APPEARANCE_PROPERTIES.register("ambient_sound", () -> new MachineAppearanceProperty<>(SoundEvent.CODEC, new SoundEvent(new ResourceLocation(""))));
    public static final RegistrySupplier<MachineAppearanceProperty<CMSoundType>> INTERACTION_SOUND_PROPERTY = APPEARANCE_PROPERTIES.register("interaction_sound", () -> new MachineAppearanceProperty<>(CMSoundType.CODEC, CMSoundType.DEFAULT));
    public static final RegistrySupplier<MachineAppearanceProperty<Integer>> LIGHT_PROPERTY = APPEARANCE_PROPERTIES.register("light", () -> new MachineAppearanceProperty<>(Codec.intRange(0, 15), 0));
    public static final RegistrySupplier<MachineAppearanceProperty<Integer>> COLOR_PROPERTY = APPEARANCE_PROPERTIES.register("color", () -> new MachineAppearanceProperty<>(Codec.INT, 0xFFFFFF));
    public static final RegistrySupplier<MachineAppearanceProperty<Float>> HARDNESS_PROPERTY = APPEARANCE_PROPERTIES.register("hardness", () -> new MachineAppearanceProperty<>(Codec.floatRange(0, Float.MAX_VALUE), 3.5F));
    public static final RegistrySupplier<MachineAppearanceProperty<Float>> RESISTANCE_PROPERTY = APPEARANCE_PROPERTIES.register("resistance", () -> new MachineAppearanceProperty<>(Codec.floatRange(0, Float.MAX_VALUE), 3.5F));
    public static final RegistrySupplier<MachineAppearanceProperty<List<TagKey<Block>>>> TOOL_TYPE_PROPERTY = APPEARANCE_PROPERTIES.register("tool_type", () -> new MachineAppearanceProperty<>(Codecs.list(TagKey.codec(Registry.BLOCK_REGISTRY)), Collections.singletonList(BlockTags.MINEABLE_WITH_PICKAXE)));
    public static final RegistrySupplier<MachineAppearanceProperty<TagKey<Block>>> MINING_LEVEL_PROPERTY = APPEARANCE_PROPERTIES.register("mining_level", () -> new MachineAppearanceProperty<>(TagKey.codec(Registry.BLOCK_REGISTRY), BlockTags.NEEDS_IRON_TOOL));
    public static final RegistrySupplier<MachineAppearanceProperty<Boolean>> REQUIRES_TOOL = APPEARANCE_PROPERTIES.register("requires_tool", () -> new MachineAppearanceProperty<>(Codec.BOOL, true));
    public static final RegistrySupplier<MachineAppearanceProperty<VoxelShape>> SHAPE_PROPERTY = APPEARANCE_PROPERTIES.register("shape", () -> new MachineAppearanceProperty<>(Codecs.VOXEL_SHAPE_CODEC, Shapes.block()));

    public static final RegistrySupplier<DataType<BooleanData, Boolean>> BOOLEAN_DATA = DATAS.register("boolean", () -> new DataType<>(Boolean.class, BooleanSyncable::create, BooleanData::new));
    public static final RegistrySupplier<DataType<IntegerData, Integer>> INTEGER_DATA = DATAS.register("integer", () -> new DataType<>(Integer.class, IntegerSyncable::create, IntegerData::new));
    public static final RegistrySupplier<DataType<DoubleData, Double>> DOUBLE_DATA = DATAS.register("double", () -> new DataType<>(Double.class, DoubleSyncable::create, DoubleData::new));
    public static final RegistrySupplier<DataType<ItemStackData, ItemStack>> ITEMSTACK_DATA = DATAS.register("itemstack", () -> new DataType<>(ItemStack.class, ItemStackSyncable::create, ItemStackData::new));
    public static final RegistrySupplier<DataType<FluidStackData, FluidStack>> FLUIDSTACK_DATA = DATAS.register("fluidstack", () -> new DataType<>(FluidStack.class, FluidStackSyncable::create, FluidStackData::new));
    public static final RegistrySupplier<DataType<StringData, String>> STRING_DATA = DATAS.register("string", () -> new DataType<>(String.class, StringSyncable::create, StringData::new));
    public static final RegistrySupplier<DataType<LongData, Long>> LONG_DATA = DATAS.register("long", () -> new DataType<>(Long.class, LongSyncable::create, LongData::new));
    public static final RegistrySupplier<DataType<SideConfigData, SideConfig>> SIDE_CONFIG_DATA = DATAS.register("side_config", () -> new DataType<>(SideConfig.class, SideConfigSyncable::create, SideConfigData::readData));

    public static final RegistrySupplier<ProcessorType<DummyProcessor>> DUMMY_PROCESSOR = PROCESSORS.register("dummy", () -> new ProcessorType<>(DummyProcessor.Template.CODEC));
    public static final RegistrySupplier<ProcessorType<MachineProcessor>> MACHINE_PROCESSOR = PROCESSORS.register("machine", () -> new ProcessorType<>(MachineProcessor.Template.CODEC));
    public static final RegistrySupplier<ProcessorType<CraftProcessor>> CRAFT_PROCESSOR = PROCESSORS.register("craft", () -> new ProcessorType<>(CraftProcessor.Template.CODEC));

    public static void registerComponentVariants() {
        ITEM_MACHINE_COMPONENT.get().addVariant(DefaultItemComponentVariant.INSTANCE);
        ITEM_MACHINE_COMPONENT.get().addVariant(FuelItemComponentVariant.INSTANCE);
        ITEM_MACHINE_COMPONENT.get().addVariant(UpgradeItemComponentVariant.INSTANCE);
        ITEM_MACHINE_COMPONENT.get().addVariant(ResultItemComponentVariant.INSTANCE);
    }
}
