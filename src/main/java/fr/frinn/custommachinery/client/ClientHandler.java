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
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.ModelAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.NumberAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.appearance.builder.TextAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.MachineComponentBuilderRegistry;
import fr.frinn.custommachinery.client.screen.creation.component.RegisterComponentBuilderEvent;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ChunkloadComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.EnergyComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ExperienceComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.FluidComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.builder.ItemComponentBuilder;
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
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.config.CMConfig;
import fr.frinn.custommachinery.impl.guielement.GuiElementWidgetSupplierRegistry;
import fr.frinn.custommachinery.impl.integration.jei.GuiElementJEIRendererRegistry;
import fr.frinn.custommachinery.impl.integration.jei.WidgetToJeiIngredientRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.ModelEvent.BakingCompleted;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@EventBusSubscriber(modid = CustomMachinery.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientHandler {

    private static Map<ModelResourceLocation, BakedModel> models;

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(Registration.CUSTOM_MACHINE_BLOCK.get(), getRenderType(Registration.CUSTOM_MACHINE_BLOCK.get().renderType));
        CustomMachinery.CUSTOM_BLOCK_MACHINES.values().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, getRenderType(block.renderType)));

        GuiElementWidgetSupplierRegistry.init();
        AppearancePropertyBuilderRegistry.init();
        MachineComponentBuilderRegistry.init();
        GuiElementBuilderRegistry.init();

        if(ModList.get().isLoaded("jei")) {
            GuiElementJEIRendererRegistry.init();
            WidgetToJeiIngredientRegistry.init();
        }

        if(ModList.get().isLoaded("cloth_config"))
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (client, parent) -> AutoConfig.getConfigScreen(CMConfig.class, parent).get());
    }

    @SubscribeEvent
    public static void registerMenuScreens(final RegisterMenuScreensEvent event) {
        event.register(Registration.CUSTOM_MACHINE_CONTAINER.get(), CustomMachineScreen::new);
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.CUSTOM_MACHINE_TILE.get(), CustomMachineRenderer::new);
    }

    @SubscribeEvent
    public static void registerGuiElementWidgets(final RegisterGuiElementWidgetSupplierEvent event) {
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

    @SubscribeEvent
    public static void registerGuiElementJEIRenderers(final RegisterGuiElementJEIRendererEvent event) {
        event.register(Registration.ENERGY_GUI_ELEMENT.get(), new EnergyGuiElementJeiRenderer());
        event.register(Registration.EXPERIENCE_GUI_ELEMENT.get(), new ExperienceGuiElementJeiRenderer());
        event.register(Registration.FLUID_GUI_ELEMENT.get(), new FluidGuiElementJeiRenderer());
        event.register(Registration.FUEL_GUI_ELEMENT.get(), new FuelGuiElementJeiRenderer());
        event.register(Registration.PROGRESS_GUI_ELEMENT.get(), new ProgressGuiElementJeiRenderer());
        event.register(Registration.SLOT_GUI_ELEMENT.get(), new SlotGuiElementJeiRenderer());
        event.register(Registration.TEXT_GUI_ELEMENT.get(), new TextGuiElementJeiRenderer());
        event.register(Registration.TEXTURE_GUI_ELEMENT.get(), new TextureGuiElementJeiRenderer());
    }

    @SubscribeEvent
    public static void registerWidgetToJeiIngredientGetters(final RegisterWidgetToJeiIngredientGetterEvent event) {
        event.register(Registration.FLUID_GUI_ELEMENT.get(), new FluidIngredientGetter());
    }

    @SubscribeEvent
    public static void registerAppearancePropertyBuilders(final RegisterAppearancePropertyBuilderEvent event) {
        event.register(Registration.BLOCK_MODEL_PROPERTY.get(), new ModelAppearancePropertyBuilder(Component.literal("Block"), Registration.BLOCK_MODEL_PROPERTY.get()));
        event.register(Registration.ITEM_MODEL_PROPERTY.get(), new ModelAppearancePropertyBuilder(Component.literal("Item"), Registration.ITEM_MODEL_PROPERTY.get()));
        //event.register(Registration.AMBIENT_SOUND_PROPERTY.get(), new TextAppearancePropertyBuilder<>(Component.literal("Ambient sound"), Registration.AMBIENT_SOUND_PROPERTY.get(), s -> SoundEvent.createFixedRangeEvent(new ResourceLocation(s), 15), SoundEvent::toString));
        //event.register(Registration.INTERACTION_SOUND_PROPERTY.get(), new TextAppearancePropertyBuilder<>(Component.literal("Interaction sound"), Registration.INTERACTION_SOUND_PROPERTY.get(), s -> new CMSoundType(new PartialBlockState(BuiltInRegistries.BLOCK.get(new ResourceLocation(s)))), CMSoundType::toString));
        event.register(Registration.LIGHT_PROPERTY.get(), new NumberAppearancePropertyBuilder<>(Component.literal("Light"), Registration.LIGHT_PROPERTY.get(), 0, 15));
        event.register(Registration.COLOR_PROPERTY.get(), new TextAppearancePropertyBuilder<>(Component.literal("Color"), Registration.COLOR_PROPERTY.get(), Integer::valueOf, Object::toString));
        event.register(Registration.HARDNESS_PROPERTY.get(), new NumberAppearancePropertyBuilder<>(Component.literal("Hardness"), Registration.HARDNESS_PROPERTY.get(), -1F, 100F));
        event.register(Registration.RESISTANCE_PROPERTY.get(), new NumberAppearancePropertyBuilder<>(Component.literal("Resistance"), Registration.RESISTANCE_PROPERTY.get(), 0F, 2000F));
    }

    @SubscribeEvent
    public static void registerMachineComponentBuilders(final RegisterComponentBuilderEvent event) {
        event.register(Registration.CHUNKLOAD_MACHINE_COMPONENT.get(), new ChunkloadComponentBuilder());
        event.register(Registration.ENERGY_MACHINE_COMPONENT.get(), new EnergyComponentBuilder());
        event.register(Registration.EXPERIENCE_MACHINE_COMPONENT.get(), new ExperienceComponentBuilder());
        event.register(Registration.FLUID_MACHINE_COMPONENT.get(), new FluidComponentBuilder());
        event.register(Registration.ITEM_MACHINE_COMPONENT.get(), new ItemComponentBuilder());
        event.register(Registration.REDSTONE_MACHINE_COMPONENT.get(), new RedstoneComponentBuilder());
    }

    @SubscribeEvent
    public static void registerGuiElementBuilders(final RegisterGuiElementBuilderEvent event) {
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

    @SubscribeEvent
    public static void registerBlockColors(final RegisterColorHandlersEvent.Block event) {
        event.register(ClientHandler::blockColor, Registration.CUSTOM_MACHINE_BLOCK.get());
        CustomMachinery.CUSTOM_BLOCK_MACHINES.values().forEach(block -> event.register(ClientHandler::blockColor, block));
    }

    @SubscribeEvent
    public static void registerItemColors(final RegisterColorHandlersEvent.Item event) {
        event.register(ClientHandler::itemColor, Registration.CUSTOM_MACHINE_ITEM::get);
        CustomMachinery.CUSTOM_BLOCK_MACHINES.values().forEach(block -> event.register(ClientHandler::itemColor, block));
    }

    @SubscribeEvent
    public static void registerModelLoader(final ModelEvent.RegisterGeometryLoaders event) {
        event.register(CustomMachinery.rl("custom_machine"), CustomMachineModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(final ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(CustomMachinery.rl("block/nope")));
        event.register(ModelResourceLocation.standalone(CustomMachinery.rl("default/custom_machine_default")));
        for(String folder : CMConfig.get().modelFolders) {
            Minecraft.getInstance().getResourceManager().listResources("models/" + folder, s -> s.getPath().endsWith(".json")).forEach((rl, resource) -> {
                ResourceLocation modelRL = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath().substring(7).replace(".json", ""));
                event.register(ModelResourceLocation.standalone(modelRL));
            });
        }
    }

    @SubscribeEvent
    public static void onBackingCompleted(final BakingCompleted event) {
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

    public static RenderType getRenderType(String renderType) {
        return switch(renderType) {
            case "solid" -> RenderType.solid();
            case "cutout" -> RenderType.cutout();
            case "translucent" -> RenderType.translucent();
            default -> throw new IllegalArgumentException("Invalid render type: " + renderType);
        };
    }

    public static int getLineHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    public static int textWidth(Component text) {
        return Minecraft.getInstance().font.width(text);
    }
}
