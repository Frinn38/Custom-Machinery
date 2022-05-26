package fr.frinn.custommachinery.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.RegisterGuiElementRendererEvent;
import fr.frinn.custommachinery.apiimpl.guielement.GuiElementRendererRegistry;
import fr.frinn.custommachinery.client.render.CustomMachineModelLoader;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.client.render.element.DumpGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.EnergyGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.FluidGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.FuelGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.PlayerInventoryGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.ProgressGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.ResetGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.SizeGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.SlotGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.StatusGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.TextGuiElementRenderer;
import fr.frinn.custommachinery.client.render.element.TextureGuiElementRenderer;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.MachineLoadingScreen;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Color3F;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomMachinery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHandler {

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(Registration.CUSTOM_MACHINE_BLOCK.get(), RenderType.translucent());
        event.enqueueWork(() -> {
            MenuScreens.register(Registration.CUSTOM_MACHINE_CONTAINER.get(), CustomMachineScreen::new);

            GuiElementRendererRegistry.init();
        });
    }

    @SubscribeEvent
    public static void registerBESR(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.CUSTOM_MACHINE_TILE.get(), CustomMachineRenderer::new);
    }

    @SubscribeEvent
    public static void modelRegistry(final ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(CustomMachinery.MODID, "custom_machine"), CustomMachineModelLoader.INSTANCE);
        ForgeModelBakery.addSpecialModel(new ResourceLocation(CustomMachinery.MODID, "block/nope"));
        Minecraft.getInstance().getResourceManager().listResources("models/machine", s -> s.endsWith(".json")).forEach(rl -> {
            ResourceLocation modelRL = new ResourceLocation(rl.getNamespace(), rl.getPath().substring(7).replace(".json", ""));
            ForgeModelBakery.addSpecialModel(modelRL);
        });
    }

    @SubscribeEvent
    public static void registerBlockColors(final ColorHandlerEvent.Block event) {
        event.getBlockColors().register((state, world, pos, tintIndex) -> {
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
                    if(tile instanceof CustomMachineTile) {
                        CustomMachineTile machineTile = (CustomMachineTile)tile;
                        return machineTile.getMachine().getAppearance(machineTile.craftingManager.getStatus()).getColor();
                    }
                default:
                    return 0xFFFFFF;
            }
        }, Registration.CUSTOM_MACHINE_BLOCK.get());
    }

    @SubscribeEvent
    public static void registerItemColors(final ColorHandlerEvent.Item event) {
        event.getItemColors().register((stack, tintIndex) -> {
            BlockState state = Registration.CUSTOM_MACHINE_BLOCK.get().defaultBlockState();
            Level world = Minecraft.getInstance().level;
            BlockPos pos = Minecraft.getInstance().player.blockPosition();
            return Minecraft.getInstance().getBlockColors().getColor(state, world, pos, tintIndex);
        }, Registration.CUSTOM_MACHINE_ITEM::get);
    }

    @SubscribeEvent
    public static void registerGuiElementRenderers(final RegisterGuiElementRendererEvent event) {
        event.register(Registration.ENERGY_GUI_ELEMENT.get(), new EnergyGuiElementRenderer());
        event.register(Registration.FLUID_GUI_ELEMENT.get(), new FluidGuiElementRenderer());
        event.register(Registration.PLAYER_INVENTORY_GUI_ELEMENT.get(), new PlayerInventoryGuiElementRenderer());
        event.register(Registration.PROGRESS_GUI_ELEMENT.get(), new ProgressGuiElementRenderer());
        event.register(Registration.SLOT_GUI_ELEMENT.get(), new SlotGuiElementRenderer());
        event.register(Registration.STATUS_GUI_ELEMENT.get(), new StatusGuiElementRenderer());
        event.register(Registration.TEXTURE_GUI_ELEMENT.get(), new TextureGuiElementRenderer());
        event.register(Registration.TEXT_GUI_ELEMENT.get(), new TextGuiElementRenderer());
        event.register(Registration.FUEL_GUI_ELEMENT.get(), new FuelGuiElementRenderer());
        event.register(Registration.RESET_GUI_ELEMENT.get(), new ResetGuiElementRenderer());
        event.register(Registration.DUMP_GUI_ELEMENT.get(), new DumpGuiElementRenderer());
        event.register(Registration.SIZE_GUI_ELEMENT.get(), new SizeGuiElementRenderer());
    }

    public static void openMachineLoadingScreen() {
        Minecraft.getInstance().setScreen(MachineLoadingScreen.INSTANCE);
    }

    public static boolean isShifting() {
        return Screen.hasControlDown();
    }

    @Nonnull
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

    @SuppressWarnings("deprecation")
    public static void renderItemOverlayIntoGUI(PoseStack matrix, Font fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
        if (!stack.isEmpty()) {
            if (stack.getCount() != 1 || text != null) {
                matrix.pushPose();
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                matrix.translate(0.0D, 0.0D, Minecraft.getInstance().getItemRenderer().blitOffset + 200.0F);
                MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                fr.drawInBatch(s, (float)(xPosition + 19 - 2 - fr.width(s)), (float)(yPosition + 6 + 3), 16777215, true, matrix.last().pose(), irendertypebuffer$impl, false, 0, 15728880);
                irendertypebuffer$impl.endBatch();
                RenderSystem.enableDepthTest();
                matrix.popPose();
            }

            if (stack.getItem().isBarVisible(stack)) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuilder();
                double health = stack.getItem().getBarWidth(stack);
                int i = Math.round(13.0F - (float)health * 13.0F);
                int j = stack.getItem().getBarColor(stack);
                draw(bufferbuilder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                draw(bufferbuilder, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            LocalPlayer clientplayerentity = Minecraft.getInstance().player;
            float f3 = clientplayerentity == null ? 0.0F : clientplayerentity.getCooldowns().getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
            if (f3 > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tesselator tessellator1 = Tesselator.getInstance();
                BufferBuilder bufferbuilder1 = tessellator1.getBuilder();
                draw(bufferbuilder1, xPosition, yPosition + Mth.floor(16.0F * (1.0F - f3)), 16, Mth.ceil(16.0F * f3), 255, 255, 255, 127);
                RenderSystem.disableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }
    }

    private static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        renderer.vertex(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex(x, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex(x + width, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex(x + width, y, 0.0D).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();
    }

    public static void renderFluidInTank(PoseStack matrix, int left, int bottom, int height, TextureAtlasSprite sprite, Color3F color) {
        bindTexture(InventoryMenu.BLOCK_ATLAS);

        int verticalAmount = height / 16;
        int verticalRemainder = height - (verticalAmount * 16);
        int top = bottom - height;

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        Matrix4f matrix4f = matrix.last().pose();

        for(int i = 0; i < verticalAmount; i++) {
            builder.vertex(matrix4f, left, top + i * 16 + 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).uv(sprite.getU0(), sprite.getV1()).endVertex();
            builder.vertex(matrix4f, left + 16, top + i * 16 + 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).uv(sprite.getU1(), sprite.getV1()).endVertex();
            builder.vertex(matrix4f, left + 16, top + i * 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).uv(sprite.getU1(), sprite.getV0()).endVertex();
            builder.vertex(matrix4f, left, top + i * 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).uv(sprite.getU0(), sprite.getV0()).endVertex();
        }

        if(verticalRemainder != 0) {
            float maxV = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * (float)verticalRemainder / 16.0F;
            builder.vertex(matrix4f, left, top + verticalAmount * 16 + verticalRemainder, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).uv(sprite.getU0(), maxV).endVertex();
            builder.vertex(matrix4f, left + 16, top + verticalAmount * 16 + verticalRemainder, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).uv(sprite.getU1(), maxV).endVertex();
            builder.vertex(matrix4f, left + 16, top + verticalAmount * 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).uv(sprite.getU1(), sprite.getV0()).endVertex();
            builder.vertex(matrix4f, left, top + verticalAmount * 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).uv(sprite.getU0(), sprite.getV0()).endVertex();
        }

        builder.end();
        BufferUploader.end(builder);
    }
/*
    public static void renderFluidInTank(MatrixStack matrix, int x, int y, int yOffset, int fluidHeight, TextureAtlasSprite sprite, Color3F color) {
        Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);

        int drawAmount = fluidHeight / 16;
        int remainder = fluidHeight - (drawAmount * 16);
        int yTop = y + yOffset;

        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        builder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        Matrix4f matrix4f = matrix.getLast().getMatrix();

        for(int i = 0; i < drawAmount; i++) {
            builder.pos(matrix4f, x, yTop + i * 16 + 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
            builder.pos(matrix4f, x + 16, yTop + i * 16 + 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
            builder.pos(matrix4f, x + 16, yTop + i * 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
            builder.pos(matrix4f, x, yTop + i * 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        }

        if(remainder != 0) {
            float maxV = sprite.getMinV() + (sprite.getMaxV() - sprite.getMinV()) * (float)remainder / 16.0F;
            builder.pos(matrix4f, x, yTop + drawAmount * 16 + remainder, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).tex(sprite.getMinU(), maxV).endVertex();
            builder.pos(matrix4f, x + 16, yTop + drawAmount * 16 + remainder, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).tex(sprite.getMaxU(), maxV).endVertex();
            builder.pos(matrix4f, x + 16, yTop + drawAmount * 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
            builder.pos(matrix4f, x, yTop + drawAmount * 16, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        }

        builder.finishDrawing();
        WorldVertexBufferUploader.draw(builder);
    }*/

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

        screen.renderTooltip(poseStack, textLines, Optional.empty(), x, y, font, ItemStack.EMPTY);
    }
}
