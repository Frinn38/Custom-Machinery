package fr.frinn.custommachinery.common.init;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
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
import fr.frinn.custommachinery.common.component.ChunkloadMachineComponent;
import fr.frinn.custommachinery.common.component.ChunkloadMachineComponent.Template;
import fr.frinn.custommachinery.common.component.CommandMachineComponent;
import fr.frinn.custommachinery.common.component.DataMachineComponent;
import fr.frinn.custommachinery.common.component.DropMachineComponent;
import fr.frinn.custommachinery.common.component.EffectMachineComponent;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.EntityMachineComponent;
import fr.frinn.custommachinery.common.component.ExperienceMachineComponent;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.FuelMachineComponent;
import fr.frinn.custommachinery.common.component.FunctionMachineComponent;
import fr.frinn.custommachinery.common.component.LightMachineComponent;
import fr.frinn.custommachinery.common.component.PositionMachineComponent;
import fr.frinn.custommachinery.common.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.component.SkyMachineComponent;
import fr.frinn.custommachinery.common.component.StructureMachineComponent;
import fr.frinn.custommachinery.common.component.TimeMachineComponent;
import fr.frinn.custommachinery.common.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.component.WorkingCoreMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.component.item.EnergyItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.FilterItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.FluidHandlerItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.FuelItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.ResultItemMachineComponent;
import fr.frinn.custommachinery.common.component.item.UpgradeItemMachineComponent;
import fr.frinn.custommachinery.common.crafting.DummyProcessor;
import fr.frinn.custommachinery.common.crafting.craft.CraftProcessor;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipeSerializer;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipeSerializer;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.guielement.BackgroundGuiElement;
import fr.frinn.custommachinery.common.guielement.BarGuiElement;
import fr.frinn.custommachinery.common.guielement.ButtonGuiElement;
import fr.frinn.custommachinery.common.guielement.ConfigGuiElement;
import fr.frinn.custommachinery.common.guielement.DumpGuiElement;
import fr.frinn.custommachinery.common.guielement.EmptyGuiElement;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.guielement.FuelGuiElement;
import fr.frinn.custommachinery.common.guielement.PlayerInventoryGuiElement;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.guielement.ResetGuiElement;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.guielement.StatusGuiElement;
import fr.frinn.custommachinery.common.guielement.TextGuiElement;
import fr.frinn.custommachinery.common.guielement.TextureGuiElement;
import fr.frinn.custommachinery.common.init.ConfigurationCardItem.ConfigurationCardData;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.network.data.BooleanData;
import fr.frinn.custommachinery.common.network.data.DoubleData;
import fr.frinn.custommachinery.common.network.data.FloatData;
import fr.frinn.custommachinery.common.network.data.FluidStackData;
import fr.frinn.custommachinery.common.network.data.IntegerData;
import fr.frinn.custommachinery.common.network.data.ItemStackData;
import fr.frinn.custommachinery.common.network.data.LongData;
import fr.frinn.custommachinery.common.network.data.NbtData;
import fr.frinn.custommachinery.common.network.data.SideConfigData;
import fr.frinn.custommachinery.common.network.data.StringData;
import fr.frinn.custommachinery.common.network.syncable.BooleanSyncable;
import fr.frinn.custommachinery.common.network.syncable.DoubleSyncable;
import fr.frinn.custommachinery.common.network.syncable.FloatSyncable;
import fr.frinn.custommachinery.common.network.syncable.FluidStackSyncable;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.common.network.syncable.ItemStackSyncable;
import fr.frinn.custommachinery.common.network.syncable.LongSyncable;
import fr.frinn.custommachinery.common.network.syncable.NbtSyncable;
import fr.frinn.custommachinery.common.network.syncable.SideConfigSyncable;
import fr.frinn.custommachinery.common.network.syncable.StringSyncable;
import fr.frinn.custommachinery.common.requirement.BiomeRequirement;
import fr.frinn.custommachinery.common.requirement.BlockRequirement;
import fr.frinn.custommachinery.common.requirement.ButtonRequirement;
import fr.frinn.custommachinery.common.requirement.ChunkloadRequirement;
import fr.frinn.custommachinery.common.requirement.CommandRequirement;
import fr.frinn.custommachinery.common.requirement.DimensionRequirement;
import fr.frinn.custommachinery.common.requirement.DropRequirement;
import fr.frinn.custommachinery.common.requirement.DurabilityRequirement;
import fr.frinn.custommachinery.common.requirement.EffectRequirement;
import fr.frinn.custommachinery.common.requirement.EnergyPerTickRequirement;
import fr.frinn.custommachinery.common.requirement.EnergyRequirement;
import fr.frinn.custommachinery.common.requirement.EntityRequirement;
import fr.frinn.custommachinery.common.requirement.ExperiencePerTickRequirement;
import fr.frinn.custommachinery.common.requirement.ExperienceRequirement;
import fr.frinn.custommachinery.common.requirement.FluidPerTickRequirement;
import fr.frinn.custommachinery.common.requirement.FluidRequirement;
import fr.frinn.custommachinery.common.requirement.FuelRequirement;
import fr.frinn.custommachinery.common.requirement.FunctionRequirement;
import fr.frinn.custommachinery.common.requirement.ItemFilterRequirement;
import fr.frinn.custommachinery.common.requirement.ItemRequirement;
import fr.frinn.custommachinery.common.requirement.ItemTransformRequirement;
import fr.frinn.custommachinery.common.requirement.LightRequirement;
import fr.frinn.custommachinery.common.requirement.LootTableRequirement;
import fr.frinn.custommachinery.common.requirement.PositionRequirement;
import fr.frinn.custommachinery.common.requirement.RedstoneRequirement;
import fr.frinn.custommachinery.common.requirement.SkyRequirement;
import fr.frinn.custommachinery.common.requirement.SpeedRequirement;
import fr.frinn.custommachinery.common.requirement.StructureRequirement;
import fr.frinn.custommachinery.common.requirement.TimeRequirement;
import fr.frinn.custommachinery.common.requirement.WeatherRequirement;
import fr.frinn.custommachinery.common.requirement.WorkingCoreRequirement;
import fr.frinn.custommachinery.common.util.CMSoundType;
import fr.frinn.custommachinery.common.util.MachineModelLocation;
import fr.frinn.custommachinery.common.util.MachineShape;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class Registration {

    public static final LootContextParamSet CUSTOM_MACHINE_LOOT_PARAMETER_SET = LootContextParamSets.register("custom_machine", builder ->
            builder.optional(LootContextParams.ORIGIN).optional(LootContextParams.BLOCK_ENTITY)
    );

    public static final DeferredRegister.Blocks                                             BLOCKS                = DeferredRegister.createBlocks(CustomMachinery.MODID);
    public static final DeferredRegister<DataComponentType<?>>                              DATA_COMPONENTS       = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CustomMachinery.MODID);
    public static final DeferredRegister.Items                                              ITEMS                 = DeferredRegister.createItems(CustomMachinery.MODID);
    public static final DeferredRegister<BlockEntityType<?>>                                TILE_ENTITIES         = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CustomMachinery.MODID);
    public static final DeferredRegister<MenuType<?>>                                       MENUS                 = DeferredRegister.create(Registries.MENU, CustomMachinery.MODID);
    public static final DeferredRegister<RecipeSerializer<?>>                               RECIPE_SERIALIZERS    = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CustomMachinery.MODID);
    public static final DeferredRegister<RecipeType<?>>                                     RECIPE_TYPES          = DeferredRegister.create(Registries.RECIPE_TYPE, CustomMachinery.MODID);
    public static final DeferredRegister<CreativeModeTab>                                   CREATIVE_TABS         = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CustomMachinery.MODID);
    public static final DeferredRegister<GuiElementType<? extends IGuiElement>>             GUI_ELEMENTS          = DeferredRegister.create(GuiElementType.REGISTRY_KEY, CustomMachinery.MODID);
    public static final DeferredRegister<MachineComponentType<? extends IMachineComponent>> MACHINE_COMPONENTS    = DeferredRegister.create(MachineComponentType.REGISTRY_KEY, CustomMachinery.MODID);
    public static final DeferredRegister<RequirementType<? extends IRequirement<?>>>        REQUIREMENTS          = DeferredRegister.create(RequirementType.REGISTRY_KEY, CustomMachinery.MODID);
    public static final DeferredRegister<MachineAppearanceProperty<?>>                      APPEARANCE_PROPERTIES = DeferredRegister.create(MachineAppearanceProperty.REGISTRY_KEY, CustomMachinery.MODID);
    public static final DeferredRegister<DataType<?, ?>>                                    DATAS                 = DeferredRegister.create(DataType.REGISTRY_KEY, CustomMachinery.MODID);
    public static final DeferredRegister<ProcessorType<?>>                                  PROCESSORS            = DeferredRegister.create(ProcessorType.REGISTRY_KEY, CustomMachinery.MODID);

    public static final Registry<GuiElementType<? extends IGuiElement>>             GUI_ELEMENT_TYPE_REGISTRY       = GUI_ELEMENTS.makeRegistry(builder -> {});
    public static final Registry<MachineComponentType<? extends IMachineComponent>> MACHINE_COMPONENT_TYPE_REGISTRY = MACHINE_COMPONENTS.makeRegistry(builder -> {});
    public static final Registry<RequirementType<? extends IRequirement<?>>>        REQUIREMENT_TYPE_REGISTRY       = REQUIREMENTS.makeRegistry(builder -> {});
    public static final Registry<MachineAppearanceProperty<?>>                      APPEARANCE_PROPERTY_REGISTRY    = APPEARANCE_PROPERTIES.makeRegistry(builder -> {});
    public static final Registry<DataType<?, ?>>                                    DATA_REGISTRY                   = DATAS.makeRegistry(builder -> {});
    public static final Registry<ProcessorType<?>>                                  PROCESSOR_REGISTRY              = PROCESSORS.makeRegistry(builder -> {});

    public static final DeferredBlock<CustomMachineBlock> CUSTOM_MACHINE_BLOCK = BLOCKS.register("custom_machine_block", CustomMachineBlock::new);

    public static final Supplier<DataComponentType<Pair<BlockPos, BlockPos>>> BOX_CREATOR_DATA = DATA_COMPONENTS.register("box_creator", () -> DataComponentType.<Pair<BlockPos, BlockPos>>builder()
            .persistent(Codec.pair(BlockPos.CODEC, BlockPos.CODEC))
            .networkSynchronized(StreamCodec.composite(BlockPos.STREAM_CODEC, Pair::getFirst, BlockPos.STREAM_CODEC, Pair::getSecond, Pair::of))
            .build()
    );
    public static final Supplier<DataComponentType<List<BlockPos>>> STRUCTURE_CREATOR_DATA = DATA_COMPONENTS.register("structure_creator", () -> DataComponentType.<List<BlockPos>>builder()
            .persistent(BlockPos.CODEC.listOf())
            .networkSynchronized(BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()))
            .build()
    );
    public static final Supplier<DataComponentType<ResourceLocation>> MACHINE_DATA = DATA_COMPONENTS.register("machine", () -> DataComponentType.<ResourceLocation>builder()
            .persistent(ResourceLocation.CODEC)
            .networkSynchronized(ResourceLocation.STREAM_CODEC)
            .build()
    );
    public static final Supplier<DataComponentType<ConfigurationCardData>> CONFIGURATION_CARD_DATA = DATA_COMPONENTS.register("configuration_card", () -> DataComponentType.<ConfigurationCardData>builder()
            .persistent(ConfigurationCardData.CODEC)
            .networkSynchronized(ConfigurationCardData.STREAM_CODEC)
            .build()
    );

    public static final DeferredItem<CustomMachineItem>     CUSTOM_MACHINE_ITEM     = ITEMS.register("custom_machine_item", () -> new CustomMachineItem(CUSTOM_MACHINE_BLOCK.get(), new Item.Properties().component(MACHINE_DATA, CustomMachine.DUMMY_ID), null));
    public static final DeferredItem<BoxCreatorItem>        BOX_CREATOR_ITEM        = ITEMS.register("box_creator_item", () -> new BoxCreatorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<StructureCreatorItem>  STRUCTURE_CREATOR_ITEM  = ITEMS.register("structure_creator", () -> new StructureCreatorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ConfigurationCardItem> CONFIGURATION_CARD_ITEM = ITEMS.register("configuration_card", () -> new ConfigurationCardItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<BlockEntityType<CustomMachineTile>> CUSTOM_MACHINE_TILE = TILE_ENTITIES.register("custom_machine_tile", () -> new BlockEntityType<>(CustomMachineTile::new, validMachineBlocks(), null));

    public static final Supplier<MenuType<CustomMachineContainer>> CUSTOM_MACHINE_CONTAINER = MENUS.register("custom_machine_container", () -> IMenuTypeExtension.create(CustomMachineContainer::new));

    public static final Supplier<CustomMachineRecipeSerializer> CUSTOM_MACHINE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("custom_machine", CustomMachineRecipeSerializer::new);
    public static final Supplier<CustomCraftRecipeSerializer> CUSTOM_CRAFT_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("custom_craft", CustomCraftRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CustomMachineRecipe>> CUSTOM_MACHINE_RECIPE = RECIPE_TYPES.register("custom_machine", () -> new RecipeType<>() {});
    public static final DeferredHolder<RecipeType<?>, RecipeType<CustomCraftRecipe>> CUSTOM_CRAFT_RECIPE = RECIPE_TYPES.register("custom_craft", () -> new RecipeType<>() {});

    public static final Supplier<CreativeModeTab> CUSTOM_MACHINE_TAB = CREATIVE_TABS.register("custom_machine", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.custommachinery.group"))
        .icon(() -> CustomMachineItem.makeMachineItem(CustomMachine.DUMMY_ID))
        .displayItems((params, output) -> {
            output.accept(BOX_CREATOR_ITEM.get());
            output.accept(STRUCTURE_CREATOR_ITEM.get());
            output.accept(CONFIGURATION_CARD_ITEM.get());
            CustomMachinery.CUSTOM_BLOCK_MACHINES.values().forEach(block -> output.accept(block.asItem()));
            CustomMachinery.MACHINES.keySet().forEach(id -> {
                if(!CustomMachinery.CUSTOM_BLOCK_MACHINES.containsKey(id))
                    output.accept(CustomMachineItem.makeMachineItem(id));
            });
        }).build()
    );

    public static final Supplier<GuiElementType<BackgroundGuiElement>>      BACKGROUND_GUI_ELEMENT       = GUI_ELEMENTS.register("background", () -> GuiElementType.create(BackgroundGuiElement.CODEC));
    public static final Supplier<GuiElementType<BarGuiElement>>             BAR_GUI_ELEMENT              = GUI_ELEMENTS.register("bar", () -> GuiElementType.create(BarGuiElement.CODEC));
    public static final Supplier<GuiElementType<ButtonGuiElement>>          BUTTON_GUI_ELEMENT           = GUI_ELEMENTS.register("button", () -> GuiElementType.create(ButtonGuiElement.CODEC));
    public static final Supplier<GuiElementType<ConfigGuiElement>>          CONFIG_GUI_ELEMENT           = GUI_ELEMENTS.register("config", () -> GuiElementType.create(ConfigGuiElement.CODEC));
    public static final Supplier<GuiElementType<DumpGuiElement>>            DUMP_GUI_ELEMENT             = GUI_ELEMENTS.register("dump", () -> GuiElementType.create(DumpGuiElement.CODEC));
    public static final Supplier<GuiElementType<EmptyGuiElement>>           EMPTY_GUI_ELEMENT            = GUI_ELEMENTS.register("empty", () -> GuiElementType.create(EmptyGuiElement.CODEC));
    public static final Supplier<GuiElementType<EnergyGuiElement>>          ENERGY_GUI_ELEMENT           = GUI_ELEMENTS.register("energy", () -> GuiElementType.create(EnergyGuiElement.CODEC));
    public static final Supplier<GuiElementType<ExperienceGuiElement>>      EXPERIENCE_GUI_ELEMENT       = GUI_ELEMENTS.register("experience", () -> GuiElementType.create(ExperienceGuiElement.CODEC));
    public static final Supplier<GuiElementType<FluidGuiElement>>           FLUID_GUI_ELEMENT            = GUI_ELEMENTS.register("fluid", () -> GuiElementType.create(FluidGuiElement.CODEC));
    public static final Supplier<GuiElementType<FuelGuiElement>>            FUEL_GUI_ELEMENT             = GUI_ELEMENTS.register("fuel", () -> GuiElementType.create(FuelGuiElement.CODEC));
    public static final Supplier<GuiElementType<PlayerInventoryGuiElement>> PLAYER_INVENTORY_GUI_ELEMENT = GUI_ELEMENTS.register("player_inventory", () -> GuiElementType.create(PlayerInventoryGuiElement.CODEC));
    public static final Supplier<GuiElementType<ProgressBarGuiElement>>     PROGRESS_GUI_ELEMENT         = GUI_ELEMENTS.register("progress", () -> GuiElementType.create(ProgressBarGuiElement.CODEC));
    public static final Supplier<GuiElementType<ResetGuiElement>>           RESET_GUI_ELEMENT            = GUI_ELEMENTS.register("reset", () -> GuiElementType.create(ResetGuiElement.CODEC));
    public static final Supplier<GuiElementType<SlotGuiElement>>            SLOT_GUI_ELEMENT             = GUI_ELEMENTS.register("slot", () -> GuiElementType.create(SlotGuiElement.CODEC));
    public static final Supplier<GuiElementType<StatusGuiElement>>          STATUS_GUI_ELEMENT           = GUI_ELEMENTS.register("status", () -> GuiElementType.create(StatusGuiElement.CODEC));
    public static final Supplier<GuiElementType<TextureGuiElement>>         TEXTURE_GUI_ELEMENT          = GUI_ELEMENTS.register("texture", () -> GuiElementType.create(TextureGuiElement.CODEC));
    public static final Supplier<GuiElementType<TextGuiElement>>            TEXT_GUI_ELEMENT             = GUI_ELEMENTS.register("text", () -> GuiElementType.create(TextGuiElement.CODEC));

    public static final Supplier<MachineComponentType<BlockMachineComponent>>       BLOCK_MACHINE_COMPONENT        = MACHINE_COMPONENTS.register("block", () -> MachineComponentType.create(BlockMachineComponent::new));
    public static final Supplier<MachineComponentType<ChunkloadMachineComponent>>   CHUNKLOAD_MACHINE_COMPONENT    = MACHINE_COMPONENTS.register("chunkload", () -> MachineComponentType.create(Template.CODEC, ChunkloadMachineComponent::new));
    public static final Supplier<MachineComponentType<CommandMachineComponent>>     COMMAND_MACHINE_COMPONENT      = MACHINE_COMPONENTS.register("command", () -> MachineComponentType.create(CommandMachineComponent::new));
    public static final Supplier<MachineComponentType<DataMachineComponent>>        DATA_MACHINE_COMPONENT         = MACHINE_COMPONENTS.register("data", () -> MachineComponentType.create(DataMachineComponent::new));
    public static final Supplier<MachineComponentType<DropMachineComponent>>        DROP_MACHINE_COMPONENT         = MACHINE_COMPONENTS.register("drop", () -> MachineComponentType.create(DropMachineComponent::new));
    public static final Supplier<MachineComponentType<EffectMachineComponent>>      EFFECT_MACHINE_COMPONENT       = MACHINE_COMPONENTS.register("effect", () -> MachineComponentType.create(EffectMachineComponent::new));
    public static final Supplier<MachineComponentType<EnergyMachineComponent>>      ENERGY_MACHINE_COMPONENT       = MACHINE_COMPONENTS.register("energy", () -> MachineComponentType.create(EnergyMachineComponent.Template.CODEC));
    public static final Supplier<MachineComponentType<EntityMachineComponent>>      ENTITY_MACHINE_COMPONENT       = MACHINE_COMPONENTS.register("entity", () -> MachineComponentType.create(EntityMachineComponent::new));
    public static final Supplier<MachineComponentType<ExperienceMachineComponent>>  EXPERIENCE_MACHINE_COMPONENT   = MACHINE_COMPONENTS.register("experience", () -> MachineComponentType.create(ExperienceMachineComponent.Template.CODEC));
    public static final Supplier<MachineComponentType<FluidMachineComponent>>       FLUID_MACHINE_COMPONENT        = MACHINE_COMPONENTS.register("fluid", () -> MachineComponentType.create(FluidMachineComponent.Template.CODEC).setNotSingle(FluidComponentHandler::new));
    public static final Supplier<MachineComponentType<FuelMachineComponent>>        FUEL_MACHINE_COMPONENT         = MACHINE_COMPONENTS.register("fuel", () -> MachineComponentType.create(FuelMachineComponent::new));
    public static final Supplier<MachineComponentType<FunctionMachineComponent>>    FUNCTION_MACHINE_COMPONENT     = MACHINE_COMPONENTS.register("function", () -> MachineComponentType.create(FunctionMachineComponent::new));
    public static final Supplier<MachineComponentType<ItemMachineComponent>>        ITEM_MACHINE_COMPONENT         = MACHINE_COMPONENTS.register("item", () -> MachineComponentType.create(ItemMachineComponent.Template.CODEC).setNotSingle(ItemComponentHandler::new));
    public static final Supplier<MachineComponentType<ItemMachineComponent>>        ITEM_FLUID_MACHINE_COMPONENT   = MACHINE_COMPONENTS.register("item_fluid", () -> MachineComponentType.create(FluidHandlerItemMachineComponent.Template.CODEC).setNotSingle(ItemComponentHandler::new));
    public static final Supplier<MachineComponentType<ItemMachineComponent>>        ITEM_ENERGY_MACHINE_COMPONENT  = MACHINE_COMPONENTS.register("item_energy", () -> MachineComponentType.create(EnergyItemMachineComponent.Template.CODEC).setNotSingle(ItemComponentHandler::new));
    public static final Supplier<MachineComponentType<ItemMachineComponent>>        ITEM_FILTER_MACHINE_COMPONENT  = MACHINE_COMPONENTS.register("item_filter", () -> MachineComponentType.create(FilterItemMachineComponent.Template.CODEC).setNotSingle(ItemComponentHandler::new));
    public static final Supplier<MachineComponentType<ItemMachineComponent>>        ITEM_UPGRADE_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("item_upgrade", () -> MachineComponentType.create(UpgradeItemMachineComponent.Template.CODEC).setNotSingle(ItemComponentHandler::new));
    public static final Supplier<MachineComponentType<ItemMachineComponent>>        ITEM_FUEL_MACHINE_COMPONENT    = MACHINE_COMPONENTS.register("item_fuel", () -> MachineComponentType.create(FuelItemMachineComponent.Template.CODEC).setNotSingle(ItemComponentHandler::new));
    public static final Supplier<MachineComponentType<ItemMachineComponent>>        ITEM_RESULT_MACHINE_COMPONENT  = MACHINE_COMPONENTS.register("item_result", () -> MachineComponentType.create(ResultItemMachineComponent.Template.CODEC).setNotSingle(ItemComponentHandler::new));
    public static final Supplier<MachineComponentType<LightMachineComponent>>       LIGHT_MACHINE_COMPONENT        = MACHINE_COMPONENTS.register("light", () -> MachineComponentType.create(LightMachineComponent::new));
    public static final Supplier<MachineComponentType<PositionMachineComponent>>    POSITION_MACHINE_COMPONENT     = MACHINE_COMPONENTS.register("position", () -> MachineComponentType.create(PositionMachineComponent::new));
    public static final Supplier<MachineComponentType<RedstoneMachineComponent>>    REDSTONE_MACHINE_COMPONENT     = MACHINE_COMPONENTS.register("redstone", () -> MachineComponentType.create(RedstoneMachineComponent.Template.CODEC, RedstoneMachineComponent::new));
    public static final Supplier<MachineComponentType<SkyMachineComponent>>         SKY_MACHINE_COMPONENT          = MACHINE_COMPONENTS.register("sky", () -> MachineComponentType.create(SkyMachineComponent::new));
    public static final Supplier<MachineComponentType<StructureMachineComponent>>   STRUCTURE_MACHINE_COMPONENT    = MACHINE_COMPONENTS.register("structure", () -> MachineComponentType.create(StructureMachineComponent::new));
    public static final Supplier<MachineComponentType<TimeMachineComponent>>        TIME_MACHINE_COMPONENT         = MACHINE_COMPONENTS.register("time", () -> MachineComponentType.create(TimeMachineComponent::new));
    public static final Supplier<MachineComponentType<WeatherMachineComponent>>     WEATHER_MACHINE_COMPONENT      = MACHINE_COMPONENTS.register("weather", () -> MachineComponentType.create(WeatherMachineComponent::new));
    public static final Supplier<MachineComponentType<WorkingCoreMachineComponent>> WORKING_CORE_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("working_core", () -> MachineComponentType.create(WorkingCoreMachineComponent::new));

    public static final Supplier<RequirementType<BiomeRequirement>>             BIOME_REQUIREMENT               = REQUIREMENTS.register("biome", () -> RequirementType.world(BiomeRequirement.CODEC));
    public static final Supplier<RequirementType<BlockRequirement>>             BLOCK_REQUIREMENT               = REQUIREMENTS.register("block", () -> RequirementType.world(BlockRequirement.CODEC));
    public static final Supplier<RequirementType<ButtonRequirement>>            BUTTON_REQUIREMENT              = REQUIREMENTS.register("button", () -> RequirementType.inventory(ButtonRequirement.CODEC));
    public static final Supplier<RequirementType<ChunkloadRequirement>>         CHUNKLOAD_REQUIREMENT           = REQUIREMENTS.register("chunkload", () -> RequirementType.inventory(ChunkloadRequirement.CODEC));
    public static final Supplier<RequirementType<CommandRequirement>>           COMMAND_REQUIREMENT             = REQUIREMENTS.register("command", () -> RequirementType.world(CommandRequirement.CODEC));
    public static final Supplier<RequirementType<DimensionRequirement>>         DIMENSION_REQUIREMENT           = REQUIREMENTS.register("dimension", () -> RequirementType.world(DimensionRequirement.CODEC));
    public static final Supplier<RequirementType<DropRequirement>>              DROP_REQUIREMENT                = REQUIREMENTS.register("drop", () -> RequirementType.world(DropRequirement.CODEC));
    public static final Supplier<RequirementType<DurabilityRequirement>>        DURABILITY_REQUIREMENT          = REQUIREMENTS.register("durability", () -> RequirementType.inventory(DurabilityRequirement.CODEC));
    public static final Supplier<RequirementType<EffectRequirement>>            EFFECT_REQUIREMENT              = REQUIREMENTS.register("effect", () -> RequirementType.world(EffectRequirement.CODEC));
    public static final Supplier<RequirementType<EnergyPerTickRequirement>>     ENERGY_PER_TICK_REQUIREMENT     = REQUIREMENTS.register("energy_per_tick", () -> RequirementType.inventory(EnergyPerTickRequirement.CODEC));
    public static final Supplier<RequirementType<EnergyRequirement>>            ENERGY_REQUIREMENT              = REQUIREMENTS.register("energy", () -> RequirementType.inventory(EnergyRequirement.CODEC));
    public static final Supplier<RequirementType<EntityRequirement>>            ENTITY_REQUIREMENT              = REQUIREMENTS.register("entity", () -> RequirementType.world(EntityRequirement.CODEC));
    public static final Supplier<RequirementType<ExperiencePerTickRequirement>> EXPERIENCE_PER_TICK_REQUIREMENT = REQUIREMENTS.register("experience_per_tick", () -> RequirementType.inventory(ExperiencePerTickRequirement.CODEC));
    public static final Supplier<RequirementType<ExperienceRequirement>>        EXPERIENCE_REQUIREMENT          = REQUIREMENTS.register("experience", () -> RequirementType.inventory(ExperienceRequirement.CODEC));
    public static final Supplier<RequirementType<FluidPerTickRequirement>>      FLUID_PER_TICK_REQUIREMENT      = REQUIREMENTS.register("fluid_per_tick", () -> RequirementType.inventory(FluidPerTickRequirement.CODEC));
    public static final Supplier<RequirementType<FluidRequirement>>             FLUID_REQUIREMENT               = REQUIREMENTS.register("fluid", () -> RequirementType.inventory(FluidRequirement.CODEC));
    public static final Supplier<RequirementType<FuelRequirement>>              FUEL_REQUIREMENT                = REQUIREMENTS.register("fuel", () -> RequirementType.inventory(FuelRequirement.CODEC));
    public static final Supplier<RequirementType<FunctionRequirement>>          FUNCTION_REQUIREMENT            = REQUIREMENTS.register("function", () -> RequirementType.world(FunctionRequirement.CODEC));
    public static final Supplier<RequirementType<ItemFilterRequirement>>        ITEM_FILTER_REQUIREMENT         = REQUIREMENTS.register("item_filter", () -> RequirementType.inventory(ItemFilterRequirement.CODEC));
    public static final Supplier<RequirementType<ItemRequirement>>              ITEM_REQUIREMENT                = REQUIREMENTS.register("item", () -> RequirementType.inventory(ItemRequirement.CODEC));
    public static final Supplier<RequirementType<ItemTransformRequirement>>     ITEM_TRANSFORM_REQUIREMENT      = REQUIREMENTS.register("item_transform", () -> RequirementType.inventory(ItemTransformRequirement.CODEC));
    public static final Supplier<RequirementType<LightRequirement>>             LIGHT_REQUIREMENT               = REQUIREMENTS.register("light", () -> RequirementType.world(LightRequirement.CODEC));
    public static final Supplier<RequirementType<LootTableRequirement>>         LOOT_TABLE_REQUIREMENT          = REQUIREMENTS.register("loot_table", () -> RequirementType.inventory(LootTableRequirement.CODEC));
    public static final Supplier<RequirementType<PositionRequirement>>          POSITION_REQUIREMENT            = REQUIREMENTS.register("position", () -> RequirementType.world(PositionRequirement.CODEC));
    public static final Supplier<RequirementType<RedstoneRequirement>>          REDSTONE_REQUIREMENT            = REQUIREMENTS.register("redstone", () -> RequirementType.world(RedstoneRequirement.CODEC));
    public static final Supplier<RequirementType<SkyRequirement>>               SKY_REQUIREMENT                 = REQUIREMENTS.register("sky", () -> RequirementType.world(SkyRequirement.CODEC));
    public static final Supplier<RequirementType<SpeedRequirement>>             SPEED_REQUIREMENT               = REQUIREMENTS.register("speed", () -> RequirementType.inventory(SpeedRequirement.CODEC));
    public static final Supplier<RequirementType<StructureRequirement>>         STRUCTURE_REQUIREMENT           = REQUIREMENTS.register("structure", () -> RequirementType.world(StructureRequirement.CODEC));
    public static final Supplier<RequirementType<TimeRequirement>>              TIME_REQUIREMENT                = REQUIREMENTS.register("time", () -> RequirementType.world(TimeRequirement.CODEC));
    public static final Supplier<RequirementType<WeatherRequirement>>           WEATHER_REQUIREMENT             = REQUIREMENTS.register("weather", () -> RequirementType.world(WeatherRequirement.CODEC));
    public static final Supplier<RequirementType<WorkingCoreRequirement>>       WORKING_CORE_REQUIREMENT        = REQUIREMENTS.register("working_core", () -> RequirementType.world(WorkingCoreRequirement.CODEC));

    public static final Supplier<MachineAppearanceProperty<SoundEvent>>           AMBIENT_SOUND_PROPERTY     = APPEARANCE_PROPERTIES.register("ambient_sound", () -> MachineAppearanceProperty.create(DefaultCodecs.SOUND_EVENT, SoundEvent.createVariableRangeEvent(ResourceLocation.parse(""))));
    public static final Supplier<MachineAppearanceProperty<MachineModelLocation>> BLOCK_MODEL_PROPERTY       = APPEARANCE_PROPERTIES.register("block", () -> MachineAppearanceProperty.create(MachineModelLocation.CODEC, MachineModelLocation.DEFAULT));
    public static final Supplier<MachineAppearanceProperty<Integer>>              COLOR_PROPERTY             = APPEARANCE_PROPERTIES.register("color", () -> MachineAppearanceProperty.create(NamedCodec.INT, 0xFFFFFF));
    public static final Supplier<MachineAppearanceProperty<Float>>                HARDNESS_PROPERTY          = APPEARANCE_PROPERTIES.register("hardness", () -> MachineAppearanceProperty.create(NamedCodec.floatRange(-1.0F, Float.MAX_VALUE), 3.5F));
    public static final Supplier<MachineAppearanceProperty<CMSoundType>>          INTERACTION_SOUND_PROPERTY = APPEARANCE_PROPERTIES.register("interaction_sound", () -> MachineAppearanceProperty.create(CMSoundType.CODEC, CMSoundType.DEFAULT));
    public static final Supplier<MachineAppearanceProperty<MachineModelLocation>> ITEM_MODEL_PROPERTY        = APPEARANCE_PROPERTIES.register("item", () -> MachineAppearanceProperty.create(MachineModelLocation.CODEC, MachineModelLocation.DEFAULT));
    public static final Supplier<MachineAppearanceProperty<Integer>>              LIGHT_PROPERTY             = APPEARANCE_PROPERTIES.register("light", () -> MachineAppearanceProperty.create(NamedCodec.intRange(0, 15), 0));
    public static final Supplier<MachineAppearanceProperty<TagKey<Block>>>        MINING_LEVEL_PROPERTY      = APPEARANCE_PROPERTIES.register("mining_level", () -> MachineAppearanceProperty.create(DefaultCodecs.tagKey(Registries.BLOCK), BlockTags.NEEDS_IRON_TOOL));
    public static final Supplier<MachineAppearanceProperty<Boolean>>              REQUIRES_TOOL              = APPEARANCE_PROPERTIES.register("requires_tool", () -> MachineAppearanceProperty.create(NamedCodec.BOOL, true));
    public static final Supplier<MachineAppearanceProperty<Float>>                RESISTANCE_PROPERTY        = APPEARANCE_PROPERTIES.register("resistance", () -> MachineAppearanceProperty.create(NamedCodec.floatRange(0.0F, Float.MAX_VALUE), 3.5F));
    public static final Supplier<MachineAppearanceProperty<MachineShape>>         SHAPE_COLLISION_PROPERTY   = APPEARANCE_PROPERTIES.register("shape_collision", () -> MachineAppearanceProperty.create(MachineShape.CODEC, MachineShape.DEFAULT_COLLISION));
    public static final Supplier<MachineAppearanceProperty<MachineShape>>         SHAPE_PROPERTY             = APPEARANCE_PROPERTIES.register("shape", () -> MachineAppearanceProperty.create(MachineShape.CODEC, MachineShape.DEFAULT));
    public static final Supplier<MachineAppearanceProperty<List<TagKey<Block>>>>  TOOL_TYPE_PROPERTY         = APPEARANCE_PROPERTIES.register("tool_type", () -> MachineAppearanceProperty.create(DefaultCodecs.tagKey(Registries.BLOCK).listOf(), Collections.singletonList(BlockTags.MINEABLE_WITH_PICKAXE)));

    public static final Supplier<DataType<BooleanData, Boolean>>       BOOLEAN_DATA     = DATAS.register("boolean", () -> DataType.create(Boolean.class, BooleanSyncable::create, BooleanData::new));
    public static final Supplier<DataType<IntegerData, Integer>>       INTEGER_DATA     = DATAS.register("integer", () -> DataType.create(Integer.class, IntegerSyncable::create, IntegerData::new));
    public static final Supplier<DataType<DoubleData, Double>>         DOUBLE_DATA      = DATAS.register("double", () -> DataType.create(Double.class, DoubleSyncable::create, DoubleData::new));
    public static final Supplier<DataType<FloatData, Float>>           FLOAT_DATA       = DATAS.register("float", () -> DataType.create(Float.class, FloatSyncable::create, FloatData::new));
    public static final Supplier<DataType<ItemStackData, ItemStack>>   ITEMSTACK_DATA   = DATAS.register("itemstack", () -> DataType.create(ItemStack.class, ItemStackSyncable::create, ItemStackData::new));
    public static final Supplier<DataType<FluidStackData, FluidStack>> FLUIDSTACK_DATA  = DATAS.register("fluidstack", () -> DataType.create(FluidStack.class, FluidStackSyncable::create, FluidStackData::new));
    public static final Supplier<DataType<StringData, String>>         STRING_DATA      = DATAS.register("string", () -> DataType.create(String.class, StringSyncable::create, StringData::new));
    public static final Supplier<DataType<LongData, Long>>             LONG_DATA        = DATAS.register("long", () -> DataType.create(Long.class, LongSyncable::create, LongData::new));
    public static final Supplier<DataType<SideConfigData, SideConfig>> SIDE_CONFIG_DATA = DATAS.register("side_config", () -> DataType.create(SideConfig.class, SideConfigSyncable::create, SideConfigData::readData));
    public static final Supplier<DataType<NbtData, CompoundTag>>       NBT_DATA         = DATAS.register("nbt", () -> DataType.create(CompoundTag.class, NbtSyncable::create, NbtData::new));

    public static final Supplier<ProcessorType<DummyProcessor>> DUMMY_PROCESSOR     = PROCESSORS.register("dummy", () -> ProcessorType.create(DummyProcessor.Template.CODEC));
    public static final Supplier<ProcessorType<MachineProcessor>> MACHINE_PROCESSOR = PROCESSORS.register("machine", () -> ProcessorType.create(MachineProcessor.Template.CODEC));
    public static final Supplier<ProcessorType<CraftProcessor>> CRAFT_PROCESSOR     = PROCESSORS.register("craft", () -> ProcessorType.create(CraftProcessor.Template.CODEC));

    private static Set<Block> validMachineBlocks() {
        Set<Block> validBlocks = new HashSet<>();
        validBlocks.add(CUSTOM_MACHINE_BLOCK.get());
        validBlocks.addAll(CustomMachinery.CUSTOM_BLOCK_MACHINES.values());
        return validBlocks;
    }
}
