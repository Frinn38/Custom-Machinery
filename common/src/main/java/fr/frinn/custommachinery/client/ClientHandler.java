package fr.frinn.custommachinery.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.architectury.event.events.client.ClientTooltipEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.RegisterGuiElementWidgetSupplierEvent;
import fr.frinn.custommachinery.api.integration.jei.RegisterGuiElementJEIRendererEvent;
import fr.frinn.custommachinery.client.integration.jei.element.EnergyGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.FluidGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.FuelGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.ProgressGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.SlotGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.TextGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.integration.jei.element.TextureGuiElementJeiRenderer;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.client.render.element.ConfigGuiElementWidget;
import fr.frinn.custommachinery.client.render.element.DumpGuiElementWidget;
import fr.frinn.custommachinery.client.render.element.EnergyGuiElementWidget;
import fr.frinn.custommachinery.client.render.element.FluidGuiElementWidget;
import fr.frinn.custommachinery.client.render.element.FuelGuiElementWidget;
import fr.frinn.custommachinery.client.render.element.PlayerInventoryGuiElementWidget;
import fr.frinn.custommachinery.client.render.element.ProgressGuiElementWidget;
import fr.frinn.custommachinery.client.render.element.ResetGuiElementWidget;
import fr.frinn.custommachinery.client.render.element.SlotGuiElementWidget;
import fr.frinn.custommachinery.client.render.element.StatusGuiElementWidget;
import fr.frinn.custommachinery.client.render.element.TextGuiElementWidget;
import fr.frinn.custommachinery.client.render.element.TextureGuiElementWidget;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.creator.MachineCreationScreen;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.upgrade.RecipeModifier;
import fr.frinn.custommachinery.impl.guielement.GuiElementWidgetSupplierRegistry;
import fr.frinn.custommachinery.impl.integration.jei.GuiElementJEIRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ClientHandler {

    public static void init() {
        ClientTooltipEvent.ITEM.register(ClientHandler::onItemTooltip);
        LifecycleEvent.SETUP.register(ClientHandler::clientSetup);
        RegisterGuiElementWidgetSupplierEvent.EVENT.register(ClientHandler::registerGuiElementWidgets);
        RegisterGuiElementJEIRendererEvent.EVENT.register(ClientHandler::registerGuiElementJEIRenderers);
    }

    private static void onItemTooltip(ItemStack stack, List<Component> lines, TooltipFlag flag) {
        CustomMachinery.UPGRADES.getUpgradesForItem(stack.getItem())
                .forEach(upgrade -> {
                    lines.addAll(upgrade.getTooltips());

                    if(Screen.hasControlDown() || Screen.hasShiftDown())
                        upgrade.getModifiers().stream().map(RecipeModifier::getTooltip).forEach(lines::add);
                });
    }

    private static void clientSetup() {
        RenderTypeRegistry.register(RenderType.translucent(), Registration.CUSTOM_MACHINE_BLOCK.get());
        MenuRegistry.registerScreenFactory(Registration.CUSTOM_MACHINE_CONTAINER.get(), CustomMachineScreen::new);
        GuiElementWidgetSupplierRegistry.init();
        if(Platform.isModLoaded("jei"))
            GuiElementJEIRendererRegistry.init();
        BlockEntityRendererRegistry.register(Registration.CUSTOM_MACHINE_TILE.get(), CustomMachineRenderer::new);
        ColorHandlerRegistry.registerBlockColors((state, world, pos, tintIndex) -> {
            if(world == null || pos == null)
                return 0;
            switch (tintIndex) {
                case 1:
                    return world.getBlockTint(pos, BiomeColors.WATER_COLOR_RESOLVER);
                case 2:
                    return world.getBlockTint(pos, BiomeColors.GRASS_COLOR_RESOLVER);
                case 3:
                    return world.getBlockTint(pos, BiomeColors.FOLIAGE_COLOR_RESOLVER);
                case 4:
                    BlockEntity tile = world.getBlockEntity(pos);
                    if(tile instanceof CustomMachineTile machineTile) {
                        return machineTile.getAppearance().getColor();
                    }
                default:
                    return 0xFFFFFF;
            }
        }, Registration.CUSTOM_MACHINE_BLOCK.get());

        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            BlockState state = Registration.CUSTOM_MACHINE_BLOCK.get().defaultBlockState();
            Level world = Minecraft.getInstance().level;
            BlockPos pos = Minecraft.getInstance().player.blockPosition();
            return Minecraft.getInstance().getBlockColors().getColor(state, world, pos, tintIndex);
        }, Registration.CUSTOM_MACHINE_ITEM::get);
    }

    private static void registerGuiElementWidgets(final RegisterGuiElementWidgetSupplierEvent event) {
        event.register(Registration.ENERGY_GUI_ELEMENT.get(), EnergyGuiElementWidget::new);
        event.register(Registration.FLUID_GUI_ELEMENT.get(), FluidGuiElementWidget::new);
        event.register(Registration.PLAYER_INVENTORY_GUI_ELEMENT.get(), PlayerInventoryGuiElementWidget::new);
        event.register(Registration.PROGRESS_GUI_ELEMENT.get(), ProgressGuiElementWidget::new);
        event.register(Registration.SLOT_GUI_ELEMENT.get(), SlotGuiElementWidget::new);
        event.register(Registration.STATUS_GUI_ELEMENT.get(), StatusGuiElementWidget::new);
        event.register(Registration.TEXTURE_GUI_ELEMENT.get(), TextureGuiElementWidget::new);
        event.register(Registration.TEXT_GUI_ELEMENT.get(), TextGuiElementWidget::new);
        event.register(Registration.FUEL_GUI_ELEMENT.get(), FuelGuiElementWidget::new);
        event.register(Registration.RESET_GUI_ELEMENT.get(), ResetGuiElementWidget::new);
        event.register(Registration.DUMP_GUI_ELEMENT.get(), DumpGuiElementWidget::new);
        event.register(Registration.CONFIG_GUI_ELEMENT.get(), ConfigGuiElementWidget::new);
    }

    private static void registerGuiElementJEIRenderers(final RegisterGuiElementJEIRendererEvent event) {
        event.register(Registration.ENERGY_GUI_ELEMENT.get(), new EnergyGuiElementJeiRenderer());
        event.register(Registration.FLUID_GUI_ELEMENT.get(), new FluidGuiElementJeiRenderer());
        event.register(Registration.FUEL_GUI_ELEMENT.get(), new FuelGuiElementJeiRenderer());
        event.register(Registration.PROGRESS_GUI_ELEMENT.get(), new ProgressGuiElementJeiRenderer());
        event.register(Registration.SLOT_GUI_ELEMENT.get(), new SlotGuiElementJeiRenderer());
        event.register(Registration.TEXT_GUI_ELEMENT.get(), new TextGuiElementJeiRenderer());
        event.register(Registration.TEXTURE_GUI_ELEMENT.get(), new TextureGuiElementJeiRenderer());
    }

    public static void openMachineLoadingScreen() {
        Minecraft.getInstance().setScreen(MachineCreationScreen.INSTANCE);
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

    public static void drawSizedString(Font font, PoseStack matrix, String string, int x, int y, int size, float maxScale, int color) {
        float stringSize = font.width(string);
        float scale = Math.min(size / stringSize, maxScale);
        matrix.pushPose();
        matrix.scale(scale, scale, 0);
        font.draw(matrix, string, x / scale, y / scale, color);
        matrix.popPose();
    }

    public static void drawCenteredString(Font font, PoseStack matrix, String string, int x, int y, int color) {
        int width = font.width(string);
        int height = font.lineHeight;
        matrix.pushPose();
        matrix.translate(-width / 2.0D, -height / 2.0D, 0);
        font.draw(matrix, string, x, y, color);
        matrix.popPose();
    }

    @SuppressWarnings("deprecation")
    public static void renderItemAndEffectsIntoGUI(PoseStack matrix, ItemStack stack, int x, int y) {
        matrix.pushPose();
        bindTexture(TextureAtlas.LOCATION_BLOCKS);
        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrix.translate((float)x, (float)y, 100.0F + Minecraft.getInstance().getItemRenderer().blitOffset);
        matrix.translate(8.0F, 8.0F, 0.0F);
        matrix.scale(1.0F, -1.0F, 1.0F);
        matrix.scale(16.0F, 16.0F, 16.0F);
        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0);
        boolean flag = !model.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }
        RenderSystem.disableDepthTest();
        Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.GUI, false, matrix, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, model);
        irendertypebuffer$impl.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }
        RenderSystem.disableBlend();
        matrix.popPose();
    }

    private static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        renderer.vertex(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex(x, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex(x + width, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex(x + width, y, 0.0D).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();
    }

    public static void bindTexture(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
    }

    public static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        drawHoveringText(poseStack, textLines, x, y, font);
    }

    public static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y, Font font) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.screen;
        if (screen == null) {
            return;
        }

        screen.renderTooltip(poseStack, textLines, Optional.empty(), x, y);
    }

    public static void renderSlotHighlight(PoseStack pose, int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        GuiComponent.fill(pose, x, y, x + width, y + height, -2130706433);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }
}
