package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.render.CustomMachineItemRenderer;
import fr.frinn.custommachinery.client.render.element.*;
import fr.frinn.custommachinery.client.render.element.jei.EnergyJEIIngredientRenderer;
import fr.frinn.custommachinery.client.render.element.jei.FluidStackIngredientRenderer;
import fr.frinn.custommachinery.client.render.element.jei.ItemStackJEIIngredientRenderer;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipeSerializer;
import fr.frinn.custommachinery.common.crafting.requirements.*;
import fr.frinn.custommachinery.common.data.builder.component.EnergyComponentBuilder;
import fr.frinn.custommachinery.common.data.builder.component.FluidComponentBuilder;
import fr.frinn.custommachinery.common.data.builder.component.ItemComponentBuilder;
import fr.frinn.custommachinery.common.data.component.*;
import fr.frinn.custommachinery.common.data.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.data.gui.*;
import fr.frinn.custommachinery.common.network.sync.*;
import fr.frinn.custommachinery.common.network.sync.data.*;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class Registration {

    public static final ItemGroup GROUP = new ItemGroup(CustomMachinery.MODID) {
        @Override
        public ItemStack createIcon() {
            ItemStack icon = new ItemStack(CUSTOM_MACHINE_ITEM.get());
            icon.getOrCreateTag().putString("id", "dummy");
            return icon;
        }

        @ParametersAreNonnullByDefault
        @Override
        public void fill(NonNullList<ItemStack> items) {
            ITEMS.getEntries().forEach(item -> item.get().fillItemGroup(this, items));
        }
    };

    public static final IRecipeType<CustomMachineRecipe> CUSTOM_MACHINE_RECIPE = new IRecipeType<CustomMachineRecipe>(){};

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CustomMachinery.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CustomMachinery.MODID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, CustomMachinery.MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, CustomMachinery.MODID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CustomMachinery.MODID);
    public static final DeferredRegister<GuiElementType<? extends IGuiElement>> GUI_ELEMENTS = DeferredRegister.create((Class)GuiElementType.class, CustomMachinery.MODID);
    public static final DeferredRegister<MachineComponentType<? extends IMachineComponent>> MACHINE_COMPONENTS = DeferredRegister.create((Class)MachineComponentType.class, CustomMachinery.MODID);
    public static final DeferredRegister<RequirementType<? extends IRequirement<?>>> REQUIREMENTS = DeferredRegister.create((Class)RequirementType.class, CustomMachinery.MODID);
    public static final DeferredRegister<DataType<?, ?>> DATA = DeferredRegister.create((Class)DataType.class, CustomMachinery.MODID);

    public static final Supplier<IForgeRegistry<GuiElementType<? extends IGuiElement>>> GUI_ELEMENT_TYPE_REGISTRY = GUI_ELEMENTS.makeRegistry("gui_element_type", RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<MachineComponentType<? extends IMachineComponent>>> MACHINE_COMPONENT_TYPE_REGISTRY = MACHINE_COMPONENTS.makeRegistry("machine_component_type", RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<RequirementType<? extends IRequirement<?>>>> REQUIREMENT_TYPE_REGISTRY = REQUIREMENTS.makeRegistry("requirement_type", RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<DataType<?, ?>>> DATA_REGISTRY = DATA.makeRegistry("data_type", RegistryBuilder::new);

    public static final RegistryObject<CustomMachineBlock> CUSTOM_MACHINE_BLOCK = BLOCKS.register("custom_machine_block", CustomMachineBlock::new);

    public static final RegistryObject<CustomMachineItem> CUSTOM_MACHINE_ITEM = ITEMS.register("custom_machine_item", () -> new CustomMachineItem(CUSTOM_MACHINE_BLOCK.get(), new Item.Properties().group(GROUP).setISTER(() -> CustomMachineItemRenderer::new)));
    public static final RegistryObject<MachineCreatorItem> MACHINE_CREATOR_ITEM = ITEMS.register("machine_creator_item", () ->  new MachineCreatorItem(new Item.Properties().group(GROUP).maxStackSize(1)));
    public static final RegistryObject<BoxCreatorItem> BOX_CREATOR_ITEM = ITEMS.register("box_creator_item", () -> new BoxCreatorItem(new Item.Properties().group(GROUP).maxStackSize(1)));

    public static final RegistryObject<TileEntityType<CustomMachineTile>> CUSTOM_MACHINE_TILE = TILE_ENTITIES.register("custom_machine_tile", () -> TileEntityType.Builder.create(CustomMachineTile::new, CUSTOM_MACHINE_BLOCK.get()).build(null));

    public static final RegistryObject<ContainerType<CustomMachineContainer>> CUSTOM_MACHINE_CONTAINER = CONTAINERS.register("custom_machine_container", () -> IForgeContainerType.create(CustomMachineContainer::new));

    public static final RegistryObject<CustomMachineRecipeSerializer> CUSTOM_MACHINE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("custom_machine", CustomMachineRecipeSerializer::new);

    public static final RegistryObject<GuiElementType<EnergyGuiElement>> ENERGY_GUI_ELEMENT = GUI_ELEMENTS.register("energy", () -> new GuiElementType<>(EnergyGuiElement.CODEC, EnergyGuiElementRenderer::new).setJeiIngredientType(() -> EnergyJEIIngredientRenderer::new));
    public static final RegistryObject<GuiElementType<FluidGuiElement>> FLUID_GUI_ELEMENT = GUI_ELEMENTS.register("fluid", () -> new GuiElementType<>(FluidGuiElement.CODEC, FluidGuiElementRenderer::new).setJeiIngredientType(() -> FluidStackIngredientRenderer::new));
    public static final RegistryObject<GuiElementType<PlayerInventoryGuiElement>> PLAYER_INVENTORY_GUI_ELEMENT = GUI_ELEMENTS.register("player_inventory", () -> new GuiElementType<>(PlayerInventoryGuiElement.CODEC, PlayerInventoryGuiElementRenderer::new));
    public static final RegistryObject<GuiElementType<ProgressBarGuiElement>> PROGRESS_GUI_ELEMENT = GUI_ELEMENTS.register("progress", () -> new GuiElementType<>(ProgressBarGuiElement.CODEC, ProgressGuiElementRenderer::new));
    public static final RegistryObject<GuiElementType<SlotGuiElement>> SLOT_GUI_ELEMENT = GUI_ELEMENTS.register("slot", () -> new GuiElementType<>(SlotGuiElement.CODEC, SlotGuiElementRenderer::new).setJeiIngredientType(() -> ItemStackJEIIngredientRenderer::new));
    public static final RegistryObject<GuiElementType<StatusGuiElement>> STATUS_GUI_ELEMENT = GUI_ELEMENTS.register("status", () -> new GuiElementType<>(StatusGuiElement.CODEC, StatusGuiElementRenderer::new));
    public static final RegistryObject<GuiElementType<TextureGuiElement>> TEXTURE_GUI_ELEMENT = GUI_ELEMENTS.register("texture", () -> new GuiElementType<>(TextureGuiElement.CODEC, TextureGuiElementRenderer::new));
    public static final RegistryObject<GuiElementType<TextGuiElement>> TEXT_GUI_ELEMENT = GUI_ELEMENTS.register("text", () -> new GuiElementType<>(TextGuiElement.CODEC, TextGuiElementRenderer::new));
    public static final RegistryObject<GuiElementType<FuelGuiElement>> FUEL_GUI_ELEMENT = GUI_ELEMENTS.register("fuel", () -> new GuiElementType<>(FuelGuiElement.CODEC, FuelGuiElementRenderer::new));

    public static final RegistryObject<MachineComponentType<EnergyMachineComponent>> ENERGY_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("energy", () -> new MachineComponentType<>(EnergyMachineComponent.Template.CODEC).setGUIBuilder(EnergyComponentBuilder::new));
    public static final RegistryObject<MachineComponentType<FluidMachineComponent>> FLUID_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("fluid", () -> new MachineComponentType<>(FluidMachineComponent.Template.CODEC).setNotSingle(FluidComponentHandler::new).setGUIBuilder(FluidComponentBuilder::new));
    public static final RegistryObject<MachineComponentType<ItemMachineComponent>> ITEM_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("item", () -> new MachineComponentType<>(ItemMachineComponent.Template.CODEC).setNotSingle(ItemComponentHandler::new).setGUIBuilder(ItemComponentBuilder::new));
    public static final RegistryObject<MachineComponentType<PositionMachineComponent>> POSITION_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("position", () -> new MachineComponentType<>(PositionMachineComponent::new));
    public static final RegistryObject<MachineComponentType<TimeMachineComponent>> TIME_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("time", () -> new MachineComponentType<>(TimeMachineComponent::new));
    public static final RegistryObject<MachineComponentType<CommandMachineComponent>> COMMAND_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("command", () -> new MachineComponentType<>(CommandMachineComponent::new));
    public static final RegistryObject<MachineComponentType<FuelMachineComponent>> FUEL_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("fuel", () -> new MachineComponentType<>(FuelMachineComponent::new));
    public static final RegistryObject<MachineComponentType<EffectMachineComponent>> EFFECT_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("effect", () -> new MachineComponentType<>(EffectMachineComponent::new));
    public static final RegistryObject<MachineComponentType<WeatherMachineComponent>> WEATHER_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("weather", () -> new MachineComponentType<>(WeatherMachineComponent::new));
    public static final RegistryObject<MachineComponentType<RedstoneMachineComponent>> REDSTONE_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("redstone", () -> new MachineComponentType<>(RedstoneMachineComponent.Template.CODEC));
    public static final RegistryObject<MachineComponentType<EntityMachineComponent>> ENTITY_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("entity", () -> new MachineComponentType<>(EntityMachineComponent::new));
    public static final RegistryObject<MachineComponentType<LightMachineComponent>> LIGHT_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("light", () -> new MachineComponentType<>(LightMachineComponent::new));
    public static final RegistryObject<MachineComponentType<BlockMachineComponent>> BLOCK_MACHINE_COMPONENT = MACHINE_COMPONENTS.register("block", () -> new MachineComponentType<>(BlockMachineComponent::new));

    public static final RegistryObject<RequirementType<ItemRequirement>> ITEM_REQUIREMENT = REQUIREMENTS.register("item", () -> new RequirementType<>(ItemRequirement.CODEC));
    public static final RegistryObject<RequirementType<EnergyRequirement>> ENERGY_REQUIREMENT = REQUIREMENTS.register("energy", () -> new RequirementType<>(EnergyRequirement.CODEC));
    public static final RegistryObject<RequirementType<EnergyPerTickRequirement>> ENERGY_PER_TICK_REQUIREMENT = REQUIREMENTS.register("energy_per_tick", () -> new RequirementType<>(EnergyPerTickRequirement.CODEC));
    public static final RegistryObject<RequirementType<FluidRequirement>> FLUID_REQUIREMENT = REQUIREMENTS.register("fluid", () -> new RequirementType<>(FluidRequirement.CODEC));
    public static final RegistryObject<RequirementType<FluidPerTickRequirement>> FLUID_PER_TICK_REQUIREMENT = REQUIREMENTS.register("fluid_per_tick", () -> new RequirementType<>(FluidPerTickRequirement.CODEC));
    public static final RegistryObject<RequirementType<PositionRequirement>> POSITION_REQUIREMENT = REQUIREMENTS.register("position", () -> new RequirementType<>(PositionRequirement.CODEC));
    public static final RegistryObject<RequirementType<TimeRequirement>> TIME_REQUIREMENT = REQUIREMENTS.register("time", () -> new RequirementType<>(TimeRequirement.CODEC));
    public static final RegistryObject<RequirementType<CommandRequirement>> COMMAND_REQUIREMENT = REQUIREMENTS.register("command", () -> new RequirementType<>(CommandRequirement.CODEC));
    public static final RegistryObject<RequirementType<FuelRequirement>> FUEL_REQUIREMENT = REQUIREMENTS.register("fuel", () -> new RequirementType<>(FuelRequirement.CODEC));
    public static final RegistryObject<RequirementType<EffectRequirement>> EFFECT_REQUIREMENT = REQUIREMENTS.register("effect", () -> new RequirementType<>(EffectRequirement.CODEC));
    public static final RegistryObject<RequirementType<WeatherRequirement>> WEATHER_REQUIREMENT = REQUIREMENTS.register("weather", () -> new RequirementType<>(WeatherRequirement.CODEC));
    public static final RegistryObject<RequirementType<RedstoneRequirement>> REDSTONE_REQUIREMENT = REQUIREMENTS.register("redstone", () -> new RequirementType<>(RedstoneRequirement.CODEC));
    public static final RegistryObject<RequirementType<EntityRequirement>> ENTITY_REQUIREMENT = REQUIREMENTS.register("entity", () -> new RequirementType<>(EntityRequirement.CODEC));
    public static final RegistryObject<RequirementType<LightRequirement>> LIGHT_REQUIREMENT = REQUIREMENTS.register("light", () -> new RequirementType<>(LightRequirement.CODEC));
    public static final RegistryObject<RequirementType<BlockRequirement>> BLOCK_REQUIREMENT = REQUIREMENTS.register("block", () -> new RequirementType<>(BlockRequirement.CODEC));
    public static final RegistryObject<RequirementType<SpeedRequirement>> SPEED_REQUIREMENT = REQUIREMENTS.register("speed", () -> new RequirementType<>(SpeedRequirement.CODEC));
    public static final RegistryObject<RequirementType<DurabilityRequirement>> DURABILITY_REQUIREMENT = REQUIREMENTS.register("durability", () -> new RequirementType<>(DurabilityRequirement.CODEC));

    public static final RegistryObject<DataType<BooleanData, Boolean>> BOOLEAN_DATA = DATA.register("boolean", () -> new DataType<>(BooleanSyncable::create, BooleanData::new));
    public static final RegistryObject<DataType<IntegerData, Integer>> INTEGER_DATA = DATA.register("integer", () -> new DataType<>(IntegerSyncable::create, IntegerData::new));
    public static final RegistryObject<DataType<DoubleData, Double>> DOUBLE_DATA = DATA.register("double", () -> new DataType<>(DoubleSyncable::create, DoubleData::new));
    public static final RegistryObject<DataType<ItemStackData, ItemStack>> ITEMSTACK_DATA = DATA.register("itemstack", () -> new DataType<>(ItemStackSyncable::create, ItemStackData::new));
    public static final RegistryObject<DataType<FluidStackData, FluidStack>> FLUIDSTACK_DATA = DATA.register("fluidstack", () -> new DataType<>(FluidStackSyncable::create, FluidStackData::new));
    public static final RegistryObject<DataType<StringData, String>> STRING_DATA = DATA.register("string", () -> new DataType<>(StringSyncable::create, StringData::new));

    public static void registerRecipeType(final RegistryEvent<IRecipeSerializer<?>> event) {
        Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(CustomMachinery.MODID, "custom_machine"), CUSTOM_MACHINE_RECIPE);
    }
}
