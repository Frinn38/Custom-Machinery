package fr.frinn.custommachinery.client;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.RegisterGuiElementWidgetSupplierEvent;
import fr.frinn.custommachinery.api.integration.jei.RegisterGuiElementJEIRendererEvent;
import fr.frinn.custommachinery.api.integration.jei.RegisterWidgetToJeiIngredientGetterEvent;
import fr.frinn.custommachinery.client.element.BarGuiElementWidget;
import fr.frinn.custommachinery.client.element.ButtonGuiElementWidget;
import fr.frinn.custommachinery.client.element.ConfigGuiElementWidget;
import fr.frinn.custommachinery.client.element.DumpGuiElementWidget;
import fr.frinn.custommachinery.client.element.EmptyGuiElementWidget;
import fr.frinn.custommachinery.client.element.EnergyGuiElementWidget;
import fr.frinn.custommachinery.client.element.ExperienceGuiElementWidget;
import fr.frinn.custommachinery.client.element.FluidGuiElementWidget;
import fr.frinn.custommachinery.client.element.FuelGuiElementWidget;
import fr.frinn.custommachinery.client.element.PlayerInventoryGuiElementWidget;
import fr.frinn.custommachinery.client.element.ProgressGuiElementWidget;
import fr.frinn.custommachinery.client.element.ResetGuiElementWidget;
import fr.frinn.custommachinery.client.element.SlotGuiElementWidget;
import fr.frinn.custommachinery.client.element.StatusGuiElementWidget;
import fr.frinn.custommachinery.client.element.TextGuiElementWidget;
import fr.frinn.custommachinery.client.element.TextureGuiElementWidget;
import fr.frinn.custommachinery.client.integration.jei.FluidIngredientGetter;
import fr.frinn.custommachinery.client.integration.jei.element.EnergyGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.ExperienceGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.FluidGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.FuelGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.ProgressGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.SlotGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.TextGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.TextureGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.model.CustomMachineModelLoader;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.AppearancePropertyBuilderRegistry;
import fr.frinn.custommachinery.client.screen.creation.appearance.RegisterAppearancePropertyBuilderEvent;
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.AmbientSoundAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.BooleanAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.ColorAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.InteractionSoundAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.MiningLevelAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.ModelAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.NumberAppearancePropertyBuilder.FloatAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.NumberAppearancePropertyBuilder.IntegerAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.ToolTypeAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.MachineComponentBuilderRegistry;
import fr.frinn.custommachinery.client.screen.creation.component.RegisterComponentBuilderEvent;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ChunkloadComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.EnergyComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ExperienceComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.FluidComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ItemComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ItemEnergyComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ItemFilterComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ItemFluidComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ItemFuelComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ItemResultComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ItemUpgradeComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.RedstoneComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderRegistry;
import fr.frinn.custommachinery.client.screen.creation.gui.RegisterGuiElementBuilderEvent;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.BarGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.ButtonGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.ConfigGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.DumpGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.EnergyGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.ExperienceGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.FluidGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.FuelGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.PlayerInventoryGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.ProgressBarGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.ResetGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.SlotGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.StatusGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.TextGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.TextureGuiElementBuilder;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.GuiElementWidgetSupplierRegistry;
import fr.frinn.custommachinery.impl.integration.jei.GuiElementJEIRendererRegistry;
import fr.frinn.custommachinery.impl.integration.jei.WidgetToJeiIngredientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Mod(value = CustomMachinery.MODID, dist = Dist.CLIENT)
public class ClientHandler {

    private static Map<ModelResourceLocation, BakedModel> models;

    public ClientHandler(final ModContainer CONTAINER, final IEventBus MOD_BUS) {
        CONTAINER.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        MOD_BUS.addListener(this::clientSetup);
        MOD_BUS.addListener(this::registerMenuScreens);
        MOD_BUS.addListener(this::registerBlockEntityRenderers);
        MOD_BUS.addListener(this::registerGuiElementWidgets);
        MOD_BUS.addListener(this::registerGuiElementJEIRenderers);
        MOD_BUS.addListener(this::registerWidgetToJeiIngredientGetters);
        MOD_BUS.addListener(this::registerAppearancePropertyBuilders);
        MOD_BUS.addListener(this::registerMachineComponentBuilders);
        MOD_BUS.addListener(this::registerGuiElementBuilders);
        MOD_BUS.addListener(this::registerBlockColors);
        MOD_BUS.addListener(this::registerItemColors);
        MOD_BUS.addListener(this::registerModelLoader);
        MOD_BUS.addListener(this::registerAdditionalModels);
        MOD_BUS.addListener(this::onBackingCompleted);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        GuiElementWidgetSupplierRegistry.init();
        AppearancePropertyBuilderRegistry.init();
        MachineComponentBuilderRegistry.init();
        GuiElementBuilderRegistry.init();

        if(ModList.get().isLoaded("jei")) {
            GuiElementJEIRendererRegistry.init();
            WidgetToJeiIngredientRegistry.init();
        }
    }

    private void registerMenuScreens(final RegisterMenuScreensEvent event) {
        event.register(Registration.CUSTOM_MACHINE_CONTAINER.get(), CustomMachineScreen::new);
    }

    private void registerBlockEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.CUSTOM_MACHINE_TILE.get(), CustomMachineRenderer::new);
    }

    private void registerGuiElementWidgets(final RegisterGuiElementWidgetSupplierEvent event) {
        event.register(Registration.BAR_GUI_ELEMENT.get(), BarGuiElementWidget::new);
        event.register(Registration.BUTTON_GUI_ELEMENT.get(), ButtonGuiElementWidget::new);
        event.register(Registration.CONFIG_GUI_ELEMENT.get(), ConfigGuiElementWidget::new);
        event.register(Registration.DUMP_GUI_ELEMENT.get(), DumpGuiElementWidget::new);
        event.register(Registration.EMPTY_GUI_ELEMENT.get(), EmptyGuiElementWidget::new);
        event.register(Registration.ENERGY_GUI_ELEMENT.get(), EnergyGuiElementWidget::new);
        event.register(Registration.EXPERIENCE_GUI_ELEMENT.get(), ExperienceGuiElementWidget::new);
        event.register(Registration.FLUID_GUI_ELEMENT.get(), FluidGuiElementWidget::new);
        event.register(Registration.FUEL_GUI_ELEMENT.get(), FuelGuiElementWidget::new);
        event.register(Registration.PLAYER_INVENTORY_GUI_ELEMENT.get(), PlayerInventoryGuiElementWidget::new);
        event.register(Registration.PROGRESS_GUI_ELEMENT.get(), ProgressGuiElementWidget::new);
        event.register(Registration.RESET_GUI_ELEMENT.get(), ResetGuiElementWidget::new);
        event.register(Registration.SLOT_GUI_ELEMENT.get(), SlotGuiElementWidget::new);
        event.register(Registration.STATUS_GUI_ELEMENT.get(), StatusGuiElementWidget::new);
        event.register(Registration.TEXT_GUI_ELEMENT.get(), TextGuiElementWidget::new);
        event.register(Registration.TEXTURE_GUI_ELEMENT.get(), TextureGuiElementWidget::new);
    }

    private void registerGuiElementJEIRenderers(final RegisterGuiElementJEIRendererEvent event) {
        event.register(Registration.ENERGY_GUI_ELEMENT.get(), new EnergyGuiElementJeiRenderer());
        event.register(Registration.EXPERIENCE_GUI_ELEMENT.get(), new ExperienceGuiElementJeiRenderer());
        event.register(Registration.FLUID_GUI_ELEMENT.get(), new FluidGuiElementJeiRenderer());
        event.register(Registration.FUEL_GUI_ELEMENT.get(), new FuelGuiElementJeiRenderer());
        event.register(Registration.PROGRESS_GUI_ELEMENT.get(), new ProgressGuiElementJeiRenderer());
        event.register(Registration.SLOT_GUI_ELEMENT.get(), new SlotGuiElementJeiRenderer());
        event.register(Registration.TEXT_GUI_ELEMENT.get(), new TextGuiElementJeiRenderer());
        event.register(Registration.TEXTURE_GUI_ELEMENT.get(), new TextureGuiElementJeiRenderer());
    }

    private void registerWidgetToJeiIngredientGetters(final RegisterWidgetToJeiIngredientGetterEvent event) {
        event.register(Registration.FLUID_GUI_ELEMENT.get(), new FluidIngredientGetter());
    }

    private void registerAppearancePropertyBuilders(final RegisterAppearancePropertyBuilderEvent event) {
        event.register(Registration.AMBIENT_SOUND_PROPERTY.get(), new AmbientSoundAppearancePropertyBuilder());
        event.register(Registration.BLOCK_MODEL_PROPERTY.get(), new ModelAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.block"), Registration.BLOCK_MODEL_PROPERTY.get()));
        event.register(Registration.COLOR_PROPERTY.get(), new ColorAppearancePropertyBuilder());
        event.register(Registration.HARDNESS_PROPERTY.get(), new FloatAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.hardness"), Registration.HARDNESS_PROPERTY.get(), -1F, 100F, Component.translatable("custommachinery.gui.creation.appearance.hardness.tooltip")));
        event.register(Registration.INTERACTION_SOUND_PROPERTY.get(), new InteractionSoundAppearancePropertyBuilder());
        event.register(Registration.ITEM_MODEL_PROPERTY.get(), new ModelAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.item"), Registration.ITEM_MODEL_PROPERTY.get()));
        event.register(Registration.KEEP_INVENTORY_PROPERTY.get(), new BooleanAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.keep_inventory"), Registration.KEEP_INVENTORY_PROPERTY.get(), Component.translatable("custommachinery.gui.creation.appearance.keep_inventory.tooltip")));
        event.register(Registration.LIGHT_PROPERTY.get(), new IntegerAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.light"), Registration.LIGHT_PROPERTY.get(), 0, 15, Component.translatable("custommachinery.gui.creation.appearance.light.tooltip")));
        event.register(Registration.MINING_LEVEL_PROPERTY.get(), new MiningLevelAppearancePropertyBuilder());
        event.register(Registration.REQUIRES_TOOL.get(), new BooleanAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.requires_tool"), Registration.REQUIRES_TOOL.get(), Component.translatable("custommachinery.gui.creation.appearance.requires_tool.tooltip")));
        event.register(Registration.RESISTANCE_PROPERTY.get(), new FloatAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.resistance"), Registration.RESISTANCE_PROPERTY.get(), 0F, 2000F, Component.translatable("custommachinery.gui.creation.appearance.resistance.tooltip")));
        event.register(Registration.TOOL_TYPE_PROPERTY.get(), new ToolTypeAppearancePropertyBuilder());
    }

    private void registerMachineComponentBuilders(final RegisterComponentBuilderEvent event) {
        event.register(Registration.CHUNKLOAD_MACHINE_COMPONENT.get(), new ChunkloadComponentBuilder());
        event.register(Registration.ENERGY_MACHINE_COMPONENT.get(), new EnergyComponentBuilder());
        event.register(Registration.EXPERIENCE_MACHINE_COMPONENT.get(), new ExperienceComponentBuilder());
        event.register(Registration.FLUID_MACHINE_COMPONENT.get(), new FluidComponentBuilder());
        event.register(Registration.ITEM_MACHINE_COMPONENT.get(), new ItemComponentBuilder());
        event.register(Registration.ITEM_FLUID_MACHINE_COMPONENT.get(), new ItemFluidComponentBuilder());
        event.register(Registration.ITEM_FILTER_MACHINE_COMPONENT.get(), new ItemFilterComponentBuilder());
        event.register(Registration.ITEM_ENERGY_MACHINE_COMPONENT.get(), new ItemEnergyComponentBuilder());
        event.register(Registration.ITEM_UPGRADE_MACHINE_COMPONENT.get(), new ItemUpgradeComponentBuilder());
        event.register(Registration.ITEM_RESULT_MACHINE_COMPONENT.get(), new ItemResultComponentBuilder());
        event.register(Registration.ITEM_FUEL_MACHINE_COMPONENT.get(), new ItemFuelComponentBuilder());
        event.register(Registration.REDSTONE_MACHINE_COMPONENT.get(), new RedstoneComponentBuilder());
    }

    private void registerGuiElementBuilders(final RegisterGuiElementBuilderEvent event) {
        event.register(Registration.BAR_GUI_ELEMENT.get(), new BarGuiElementBuilder());
        event.register(Registration.BUTTON_GUI_ELEMENT.get(), new ButtonGuiElementBuilder());
        event.register(Registration.CONFIG_GUI_ELEMENT.get(), new ConfigGuiElementBuilder());
        event.register(Registration.DUMP_GUI_ELEMENT.get(), new DumpGuiElementBuilder());
        event.register(Registration.ENERGY_GUI_ELEMENT.get(), new EnergyGuiElementBuilder());
        event.register(Registration.EXPERIENCE_GUI_ELEMENT.get(), new ExperienceGuiElementBuilder());
        event.register(Registration.FLUID_GUI_ELEMENT.get(), new FluidGuiElementBuilder());
        event.register(Registration.FUEL_GUI_ELEMENT.get(), new FuelGuiElementBuilder());
        event.register(Registration.PLAYER_INVENTORY_GUI_ELEMENT.get(), new PlayerInventoryGuiElementBuilder());
        event.register(Registration.PROGRESS_GUI_ELEMENT.get(), new ProgressBarGuiElementBuilder());
        event.register(Registration.RESET_GUI_ELEMENT.get(), new ResetGuiElementBuilder());
        event.register(Registration.SLOT_GUI_ELEMENT.get(), new SlotGuiElementBuilder());
        event.register(Registration.STATUS_GUI_ELEMENT.get(), new StatusGuiElementBuilder());
        event.register(Registration.TEXT_GUI_ELEMENT.get(), new TextGuiElementBuilder());
        event.register(Registration.TEXTURE_GUI_ELEMENT.get(), new TextureGuiElementBuilder());
    }

    private void registerBlockColors(final RegisterColorHandlersEvent.Block event) {
        event.register(ClientHandler::blockColor, Registration.CUSTOM_MACHINE_BLOCK.get());
        CustomMachinery.CUSTOM_BLOCK_MACHINES.values().forEach(block -> event.register(ClientHandler::blockColor, block));
    }

    private void registerItemColors(final RegisterColorHandlersEvent.Item event) {
        event.register(ClientHandler::itemColor, Registration.CUSTOM_MACHINE_ITEM::get);
        CustomMachinery.CUSTOM_BLOCK_MACHINES.values().forEach(block -> event.register(ClientHandler::itemColor, block));
    }

    private void registerModelLoader(final ModelEvent.RegisterGeometryLoaders event) {
        event.register(CustomMachinery.rl("custom_machine"), CustomMachineModelLoader.INSTANCE);
    }

    private void registerAdditionalModels(final ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(CustomMachinery.rl("block/nope")));
        event.register(ModelResourceLocation.standalone(CustomMachinery.rl("default/custom_machine_default")));
        for(String folder : CMConfig.CONFIG.modelFolders.get()) {
            Minecraft.getInstance().getResourceManager().listResources("models/" + folder, s -> s.getPath().endsWith(".json")).forEach((rl, resource) -> {
                ResourceLocation modelRL = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath().substring(7).replace(".json", ""));
                event.register(ModelResourceLocation.standalone(modelRL));
            });
        }
    }

    private void onBackingCompleted(final BakingCompleted event) {
        models = event.getModels();
    }

    public static Map<ModelResourceLocation, BakedModel> getAllModels() {
        return models;
    }

    private static int blockColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        if(level == null || pos == null)
            return 0;
        switch (tintIndex) {
            case 1:
                return level.getBlockTint(pos, BiomeColors.WATER_COLOR_RESOLVER);
            case 2:
                return level.getBlockTint(pos, BiomeColors.GRASS_COLOR_RESOLVER);
            case 3:
                return level.getBlockTint(pos, BiomeColors.FOLIAGE_COLOR_RESOLVER);
            case 4:
                BlockEntity tile = level.getBlockEntity(pos);
                if(tile instanceof CustomMachineTile machineTile) {
                    return machineTile.getAppearance().getColor();
                }
            default:
                return 0xFFFFFF;
        }
    }

    private static int itemColor(ItemStack stack, int tintIndex) {
        BlockState state = Registration.CUSTOM_MACHINE_BLOCK.get().defaultBlockState();
        Level level = Minecraft.getInstance().level;
        if(Minecraft.getInstance().player == null)
            return 0;
        BlockPos pos = Minecraft.getInstance().player.blockPosition();
        return Minecraft.getInstance().getBlockColors().getColor(state, level, pos, tintIndex);
    }

    @NotNull
    public static CustomMachineTile getClientSideCustomMachineTile(BlockPos pos) {
        if(Minecraft.getInstance().level != null) {
            BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile)
                return (CustomMachineTile)tile;
        }
        throw new IllegalStateException("Trying to open a Custom Machine container without clicking on a Custom Machine block");
    }

    public static void renderSlotHighlight(GuiGraphics graphics, int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        graphics.pose().pushPose();
        //Translate to z=110 because fluid texture render at z=100 (See FluidRenderer)
        graphics.pose().translate(0, 0, 110);
        graphics.fill(x, y, x + width, y + height, -2130706433);
        graphics.pose().popPose();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    public static boolean isShiftKeyDown() {
        return Screen.hasShiftDown();
    }

    public static int getLineHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    public static int textWidth(Component text) {
        return Minecraft.getInstance().font.width(text);
    }
}
