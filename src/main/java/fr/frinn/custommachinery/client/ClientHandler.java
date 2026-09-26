package fr.frinn.custommachinery.client;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.RegisterGuiElementWidgetSupplierEvent;
import fr.frinn.custommachinery.api.integration.jei.RegisterGuiElementJEIRendererEvent;
import fr.frinn.custommachinery.api.integration.jei.RegisterWidgetToJeiIngredientGetterEvent;
import fr.frinn.custommachinery.api.machine.MachineStatus;
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
import fr.frinn.custommachinery.client.element.SplitButtonGuiElementWidget;
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
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineTooltipComponent;
import fr.frinn.custommachinery.client.screen.creation.MachineTooltipComponent.ClientMachineTooltipComponent;
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
import fr.frinn.custommachinery.client.screen.creation.gui.builder.SplitButtonGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.StatusGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.TextGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.builder.TextureGuiElementBuilder;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.impl.component.config.SideConfig.SpriteData;
import fr.frinn.custommachinery.impl.guielement.GuiElementWidgetSupplierRegistry;
import fr.frinn.custommachinery.impl.integration.jei.GuiElementJEIRendererRegistry;
import fr.frinn.custommachinery.impl.integration.jei.WidgetToJeiIngredientRegistry;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.ModelBakery.BakingResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
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
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

@Mod(value = CustomMachinery.MODID, dist = Dist.CLIENT)
public class ClientHandler {

    @Nullable
    private static BakingResult models;

    public static final ModelProperty<MachineAppearance> APPEARANCE = new ModelProperty<>();
    public static final ModelProperty<MachineStatus> STATUS = new ModelProperty<>();

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
        MOD_BUS.addListener(this::onBackingCompleted);
        MOD_BUS.addListener(this::registerClientTooltipComponents);
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
        event.register(CMRegistration.CUSTOM_MACHINE_CONTAINER.get(), CustomMachineScreen::new);
    }

    private void registerBlockEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CMRegistration.CUSTOM_MACHINE_TILE.get(), CustomMachineRenderer::new);
    }

    private void registerGuiElementWidgets(final RegisterGuiElementWidgetSupplierEvent event) {
        event.register(CMRegistration.BAR_GUI_ELEMENT.get(), BarGuiElementWidget::new);
        event.register(CMRegistration.BUTTON_GUI_ELEMENT.get(), ButtonGuiElementWidget::new);
        event.register(CMRegistration.CONFIG_GUI_ELEMENT.get(), ConfigGuiElementWidget::new);
        event.register(CMRegistration.DUMP_GUI_ELEMENT.get(), DumpGuiElementWidget::new);
        event.register(CMRegistration.EMPTY_GUI_ELEMENT.get(), EmptyGuiElementWidget::new);
        event.register(CMRegistration.ENERGY_GUI_ELEMENT.get(), EnergyGuiElementWidget::new);
        event.register(CMRegistration.EXPERIENCE_GUI_ELEMENT.get(), ExperienceGuiElementWidget::new);
        event.register(CMRegistration.FLUID_GUI_ELEMENT.get(), FluidGuiElementWidget::new);
        event.register(CMRegistration.FUEL_GUI_ELEMENT.get(), FuelGuiElementWidget::new);
        event.register(CMRegistration.PLAYER_INVENTORY_GUI_ELEMENT.get(), PlayerInventoryGuiElementWidget::new);
        event.register(CMRegistration.PROGRESS_GUI_ELEMENT.get(), ProgressGuiElementWidget::new);
        event.register(CMRegistration.RESET_GUI_ELEMENT.get(), ResetGuiElementWidget::new);
        event.register(CMRegistration.SLOT_GUI_ELEMENT.get(), SlotGuiElementWidget::new);
        event.register(CMRegistration.SPLIT_GUI_ELEMENT.get(), SplitButtonGuiElementWidget::new);
        event.register(CMRegistration.STATUS_GUI_ELEMENT.get(), StatusGuiElementWidget::new);
        event.register(CMRegistration.TEXT_GUI_ELEMENT.get(), TextGuiElementWidget::new);
        event.register(CMRegistration.TEXTURE_GUI_ELEMENT.get(), TextureGuiElementWidget::new);
    }

    private void registerGuiElementJEIRenderers(final RegisterGuiElementJEIRendererEvent event) {
        event.register(CMRegistration.ENERGY_GUI_ELEMENT.get(), new EnergyGuiElementJeiRenderer());
        event.register(CMRegistration.EXPERIENCE_GUI_ELEMENT.get(), new ExperienceGuiElementJeiRenderer());
        event.register(CMRegistration.FLUID_GUI_ELEMENT.get(), new FluidGuiElementJeiRenderer());
        event.register(CMRegistration.FUEL_GUI_ELEMENT.get(), new FuelGuiElementJeiRenderer());
        event.register(CMRegistration.PROGRESS_GUI_ELEMENT.get(), new ProgressGuiElementJeiRenderer());
        event.register(CMRegistration.SLOT_GUI_ELEMENT.get(), new SlotGuiElementJeiRenderer());
        event.register(CMRegistration.TEXT_GUI_ELEMENT.get(), new TextGuiElementJeiRenderer());
        event.register(CMRegistration.TEXTURE_GUI_ELEMENT.get(), new TextureGuiElementJeiRenderer());
    }

    private void registerWidgetToJeiIngredientGetters(final RegisterWidgetToJeiIngredientGetterEvent event) {
        event.register(CMRegistration.FLUID_GUI_ELEMENT.get(), new FluidIngredientGetter());
    }

    private void registerAppearancePropertyBuilders(final RegisterAppearancePropertyBuilderEvent event) {
        event.register(CMRegistration.AMBIENT_SOUND_PROPERTY.get(), new AmbientSoundAppearancePropertyBuilder());
        event.register(CMRegistration.BLOCK_MODEL_PROPERTY.get(), new ModelAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.block"), CMRegistration.BLOCK_MODEL_PROPERTY.get()));
        event.register(CMRegistration.COLOR_PROPERTY.get(), new ColorAppearancePropertyBuilder());
        event.register(CMRegistration.HARDNESS_PROPERTY.get(), new FloatAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.hardness"), CMRegistration.HARDNESS_PROPERTY.get(), -1F, 100F, Component.translatable("custommachinery.gui.creation.appearance.hardness.tooltip")));
        event.register(CMRegistration.INTERACTION_SOUND_PROPERTY.get(), new InteractionSoundAppearancePropertyBuilder());
        event.register(CMRegistration.ITEM_MODEL_PROPERTY.get(), new ModelAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.item"), CMRegistration.ITEM_MODEL_PROPERTY.get()));
        event.register(CMRegistration.KEEP_INVENTORY_PROPERTY.get(), new BooleanAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.keep_inventory"), CMRegistration.KEEP_INVENTORY_PROPERTY.get(), Component.translatable("custommachinery.gui.creation.appearance.keep_inventory.tooltip")));
        event.register(CMRegistration.LIGHT_PROPERTY.get(), new IntegerAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.light"), CMRegistration.LIGHT_PROPERTY.get(), 0, 15, Component.translatable("custommachinery.gui.creation.appearance.light.tooltip")));
        event.register(CMRegistration.MINING_LEVEL_PROPERTY.get(), new MiningLevelAppearancePropertyBuilder());
        event.register(CMRegistration.REQUIRES_TOOL.get(), new BooleanAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.requires_tool"), CMRegistration.REQUIRES_TOOL.get(), Component.translatable("custommachinery.gui.creation.appearance.requires_tool.tooltip")));
        event.register(CMRegistration.RESISTANCE_PROPERTY.get(), new FloatAppearancePropertyBuilder(Component.translatable("custommachinery.gui.creation.appearance.resistance"), CMRegistration.RESISTANCE_PROPERTY.get(), 0F, 2000F, Component.translatable("custommachinery.gui.creation.appearance.resistance.tooltip")));
        event.register(CMRegistration.TOOL_TYPE_PROPERTY.get(), new ToolTypeAppearancePropertyBuilder());
    }

    private void registerMachineComponentBuilders(final RegisterComponentBuilderEvent event) {
        event.register(CMRegistration.CHUNKLOAD_MACHINE_COMPONENT.get(), new ChunkloadComponentBuilder());
        event.register(CMRegistration.ENERGY_MACHINE_COMPONENT.get(), new EnergyComponentBuilder());
        event.register(CMRegistration.EXPERIENCE_MACHINE_COMPONENT.get(), new ExperienceComponentBuilder());
        event.register(CMRegistration.FLUID_MACHINE_COMPONENT.get(), new FluidComponentBuilder());
        event.register(CMRegistration.ITEM_MACHINE_COMPONENT.get(), new ItemComponentBuilder());
        event.register(CMRegistration.ITEM_FLUID_MACHINE_COMPONENT.get(), new ItemFluidComponentBuilder());
        event.register(CMRegistration.ITEM_FILTER_MACHINE_COMPONENT.get(), new ItemFilterComponentBuilder());
        event.register(CMRegistration.ITEM_ENERGY_MACHINE_COMPONENT.get(), new ItemEnergyComponentBuilder());
        event.register(CMRegistration.ITEM_UPGRADE_MACHINE_COMPONENT.get(), new ItemUpgradeComponentBuilder());
        event.register(CMRegistration.ITEM_RESULT_MACHINE_COMPONENT.get(), new ItemResultComponentBuilder());
        event.register(CMRegistration.ITEM_FUEL_MACHINE_COMPONENT.get(), new ItemFuelComponentBuilder());
        event.register(CMRegistration.REDSTONE_MACHINE_COMPONENT.get(), new RedstoneComponentBuilder());
    }

    private void registerGuiElementBuilders(final RegisterGuiElementBuilderEvent event) {
        event.register(CMRegistration.BAR_GUI_ELEMENT.get(), new BarGuiElementBuilder());
        event.register(CMRegistration.BUTTON_GUI_ELEMENT.get(), new ButtonGuiElementBuilder());
        event.register(CMRegistration.CONFIG_GUI_ELEMENT.get(), new ConfigGuiElementBuilder());
        event.register(CMRegistration.DUMP_GUI_ELEMENT.get(), new DumpGuiElementBuilder());
        event.register(CMRegistration.ENERGY_GUI_ELEMENT.get(), new EnergyGuiElementBuilder());
        event.register(CMRegistration.EXPERIENCE_GUI_ELEMENT.get(), new ExperienceGuiElementBuilder());
        event.register(CMRegistration.FLUID_GUI_ELEMENT.get(), new FluidGuiElementBuilder());
        event.register(CMRegistration.FUEL_GUI_ELEMENT.get(), new FuelGuiElementBuilder());
        event.register(CMRegistration.PLAYER_INVENTORY_GUI_ELEMENT.get(), new PlayerInventoryGuiElementBuilder());
        event.register(CMRegistration.PROGRESS_GUI_ELEMENT.get(), new ProgressBarGuiElementBuilder());
        event.register(CMRegistration.RESET_GUI_ELEMENT.get(), new ResetGuiElementBuilder());
        event.register(CMRegistration.SLOT_GUI_ELEMENT.get(), new SlotGuiElementBuilder());
        event.register(CMRegistration.SPLIT_GUI_ELEMENT.get(), new SplitButtonGuiElementBuilder());
        event.register(CMRegistration.STATUS_GUI_ELEMENT.get(), new StatusGuiElementBuilder());
        event.register(CMRegistration.TEXT_GUI_ELEMENT.get(), new TextGuiElementBuilder());
        event.register(CMRegistration.TEXTURE_GUI_ELEMENT.get(), new TextureGuiElementBuilder());
    }

    private void registerBlockColors(final RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(Collections.singletonList(ClientHandler.blockColor()), CMRegistration.CUSTOM_MACHINE_BLOCK.get());
        CustomMachinery.CUSTOM_BLOCK_MACHINES.values().forEach(block -> event.register(Collections.singletonList(ClientHandler.blockColor()), block));
    }

    private void onBackingCompleted(final ModelEvent.BakingCompleted event) {
        models = event.getBakingResult();
    }

    public static BakingResult getAllModels() {
        if(models == null)
            throw new IllegalStateException("Trying to get models before baking completed");
        return models;
    }

    private void registerClientTooltipComponents(final RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(MachineTooltipComponent.class, ClientMachineTooltipComponent::new);
    }

    private static BlockTintSource blockColor() {
        return new BlockTintSource() {
            @Override
            public int color(BlockState blockState) {
                return 0;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                BlockEntity tile = level.getBlockEntity(pos);
                if(tile instanceof CustomMachineTile machineTile) {
                    return switch (machineTile.getAppearance().getColor()) {
                        case "water" -> level.getBlockTint(pos, BiomeColors.WATER_COLOR_RESOLVER);
                        case "grass" -> level.getBlockTint(pos, BiomeColors.GRASS_COLOR_RESOLVER);
                        case "foliage" -> level.getBlockTint(pos, BiomeColors.FOLIAGE_COLOR_RESOLVER);
                        default -> Integer.parseInt(machineTile.getAppearance().getColor());
                    };
                }
                return this.color(state);
            }
        };
    }

    public static CustomMachineTile getClientSideCustomMachineTile(BlockPos pos) {
        if(Minecraft.getInstance().level != null) {
            BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile)
                return (CustomMachineTile)tile;
        }
        throw new IllegalStateException("Trying to open a Custom Machine container without clicking on a Custom Machine block");
    }

    public static void renderSlotHighlight(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot_highlight_back"), x, y, width, height);
    }

    public static int getLineHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    public static int textWidth(Component text) {
        return Minecraft.getInstance().font.width(text);
    }

    public static void renderOrientedProgressTextures(GuiGraphicsExtractor graphics, TextureInfo emptyTexture, TextureInfo filledTexture, int x, int y, int width, int height, double percent, Orientation orientation) {
        int filledWidth = (int)(width * Mth.clamp(percent, 0.0D, 1.0D));
        int filledHeight = (int)(height * Mth.clamp(percent, 0.0D, 1.0D));

        graphics.blit(emptyTexture.texture(), x, y, emptyTexture.u(), emptyTexture.v(), width, height, width, height);
        switch (orientation) {
            case RIGHT -> graphics.blit(filledTexture.texture(), x, y, filledTexture.u(), filledTexture.v(), filledWidth, height, width, height);
            case LEFT -> graphics.blit(filledTexture.texture(), x + width - filledWidth, y, filledTexture.u() + width - filledWidth, filledTexture.v(), filledWidth, height, width, height);
            case BOTTOM -> graphics.blit(filledTexture.texture(), x, y, filledTexture.u(), filledTexture.v(), width, filledHeight, width, height);
            case TOP -> graphics.blit(filledTexture.texture(), x, y + height - filledHeight, filledTexture.u(), filledTexture.v() + height - filledHeight, width, filledHeight, width, height);
        }
    }

    public static void blit(GuiGraphicsExtractor graphics, TextureInfo texture, int x, int y, int width, int height) {
        graphics.blit(texture.texture(), x, y, texture.u(), texture.v(), width, height, width, height);
    }

    public static boolean isOverlapping(Rect2i first, Rect2i second) {
        return first.getX() <= second.getX() + second.getWidth() && first.getX() + first.getWidth() >= second.getX() && first.getY() <= second.getY() + second.getHeight() && first.getY() + first.getHeight() >= second.getY();
    }

    public static void drawDottedRect(GuiGraphicsExtractor g, int x, int y, int width, int height, int color, int dashLength, int gapLength, int offset) {
        drawDottedLine(g, x, y, x + width, y, color, dashLength, gapLength, offset); // top
        drawDottedLine(g, x + width, y, x + width, y + height, color, dashLength, gapLength, offset); // right
        drawDottedLine(g, x + width, y + height, x, y + height, color, dashLength, gapLength, offset); // bottom
        drawDottedLine(g, x, y + height, x, y, color, dashLength, gapLength, offset); // left
    }

    public static void drawDottedLine(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int argb, int dashLen, int gapLen, int offset) {
        int total = dashLen + gapLen;

        if (y1 == y2) { // horizontal
            int step = x1 < x2 ? 1 : -1;
            int len = Math.abs(x2 - x1);
            int x = x1;
            for (int i = 0; i < len; i++) {
                int phase = (i + offset) % total;
                if (phase < dashLen) {
                    g.fill(x, y1, x + step, y1 + 1, argb);
                }
                x += step;
            }
        } else if (x1 == x2) { // vertical
            int step = y1 < y2 ? 1 : -1;
            int len = Math.abs(y2 - y1);
            int y = y1;
            for (int i = 0; i < len; i++) {
                int phase = (i + offset) % total;
                if (phase < dashLen) {
                    g.fill(x1, y, x1 + 1, y + step, argb);
                }
                y += step;
            }
        }
    }

    public static void refreshMachineContainer() {
        if(Minecraft.getInstance().player instanceof Player player && player.containerMenu instanceof CustomMachineContainer container)
            container.init();
    }

    public static void renderScrollingStringNoShadow(GuiGraphicsExtractor guiGraphics, Font font, Component text, int minX, int maxX, int y, int color) {
        int width = font.width(text);
        int maxWidth = maxX - minX;
        if (width > maxWidth) {
            int l = width - maxWidth;
            double d0 = (double) Util.getMillis() / 1000.0;
            double d1 = Math.max((double)l * 0.5, 3.0);
            double d2 = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d0 / d1)) / 2.0 + 0.5;
            double d3 = Mth.lerp(d2, 0.0, l);
            guiGraphics.enableScissor(minX, y, maxX, y + font.lineHeight);
            guiGraphics.text(font, text, minX - (int)d3, y, color, false);
            guiGraphics.disableScissor();
        } else {
            int i1 = Mth.clamp(minX, minX + width / 2, maxX - width / 2);
            guiGraphics.text(font, text, i1 - font.width(text) / 2, y, color, false);
        }
    }

    public static WidgetSprites dataToSprite(SpriteData data) {
        return new WidgetSprites(data.texture(), data.textureHovered());
    }
}
