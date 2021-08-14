package fr.frinn.custommachinery.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.render.CustomMachineBakedModel;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.MachineLoadingScreen;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Color3F;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomMachinery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHandler {

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistry.bindTileEntityRenderer(Registration.CUSTOM_MACHINE_TILE.get(), CustomMachineRenderer::new);

            ScreenManager.registerFactory(Registration.CUSTOM_MACHINE_CONTAINER.get(), CustomMachineScreen::new);

            RenderTypeLookup.setRenderLayer(Registration.CUSTOM_MACHINE_BLOCK.get(), RenderType.getTranslucent());
        });
    }

    @SubscribeEvent
    public static void modelRegistry(final ModelRegistryEvent event) {
        ModelLoader.addSpecialModel(new ResourceLocation(CustomMachinery.MODID, "block/custom_machine_block"));
        ModelLoader.addSpecialModel(new ResourceLocation(CustomMachinery.MODID, "block/nope"));
        Minecraft.getInstance().getResourceManager().getAllResourceLocations("models/machine", s -> s.endsWith(".json")).forEach(rl -> {
            ResourceLocation modelRL = new ResourceLocation(rl.getNamespace(), rl.getPath().substring(7).replace(".json", ""));
            ModelLoader.addSpecialModel(modelRL);
        });
    }

    @SubscribeEvent
    public static void modelBake(final ModelBakeEvent event) {
        IBakedModel model = CustomMachineBakedModel.INSTANCE;
        Registration.CUSTOM_MACHINE_BLOCK.get().getStateContainer().getValidStates().forEach(state -> {
            ModelResourceLocation modelLocation = BlockModelShapes.getModelLocation(state);
            event.getModelRegistry().put(modelLocation, model);
        });
        event.getModelRegistry().put(new ModelResourceLocation(Registration.CUSTOM_MACHINE_ITEM.getId(), "inventory"), model);
    }

    @SubscribeEvent
    public static void registerBlockColors(final ColorHandlerEvent.Block event) {
        event.getBlockColors().register((state, world, pos, tintIndex) -> {
            if(world == null || pos == null)
                return 0;
            switch (tintIndex) {
                case 1:
                    return world.getBlockColor(pos, BiomeColors.WATER_COLOR);
                case 2:
                    return world.getBlockColor(pos, BiomeColors.GRASS_COLOR);
                case 3:
                    return world.getBlockColor(pos, BiomeColors.FOLIAGE_COLOR);
                case 4:
                    TileEntity tile = world.getTileEntity(pos);
                    if(tile instanceof CustomMachineTile)
                        return ((CustomMachineTile)tile).getMachine().getAppearance().getColor();
                default:
                    return 0xFFFFFF;
            }
        }, Registration.CUSTOM_MACHINE_BLOCK.get());
    }

    @SubscribeEvent
    public static void registerItemColors(final ColorHandlerEvent.Item event) {
        event.getItemColors().register((stack, tintIndex) -> {
            BlockState state = Registration.CUSTOM_MACHINE_BLOCK.get().getDefaultState();
            World world = Minecraft.getInstance().world;
            BlockPos pos = Minecraft.getInstance().player.getPosition();
            return Minecraft.getInstance().getBlockColors().getColor(state, world, pos, tintIndex);
        }, Registration.CUSTOM_MACHINE_ITEM::get);
    }

    public static void openMachineLoadingScreen() {
        Minecraft.getInstance().displayGuiScreen(MachineLoadingScreen.INSTANCE);
    }

    @Nonnull
    public static CustomMachineTile getClientSideCustomMachineTile(BlockPos pos) {
        if(Minecraft.getInstance().world != null) {
            TileEntity tile = Minecraft.getInstance().world.getTileEntity(pos);
            if(tile instanceof CustomMachineTile)
                return (CustomMachineTile)tile;
        }
        throw new IllegalStateException("Trying to open a Custom Machine container without clicking on a Custom Machine block");
    }

    public static void drawSizedString(FontRenderer font, MatrixStack matrix, String string, int x, int y, int size, float maxScale, int color) {
        float stringSize = font.getStringWidth(string);
        float scale = Math.min(size / stringSize, maxScale);
        matrix.push();
        matrix.scale(scale, scale, 0);
        font.drawString(matrix, string, x / scale, y / scale, color);
        matrix.pop();
    }

    public static void drawCenteredString(FontRenderer font, MatrixStack matrix, String string, int x, int y, int color) {
        int width = font.getStringWidth(string);
        int height = font.FONT_HEIGHT;
        matrix.push();
        matrix.translate(-width / 2.0D, -height / 2.0D, 0);
        font.drawString(matrix, string, x, y, color);
        matrix.pop();
    }

    @SuppressWarnings("deprecation")
    public static void renderItemAndEffectsIntoGUI(MatrixStack matrix, ItemStack stack, int x, int y) {
        matrix.push();
        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmapDirect(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        matrix.translate((float)x, (float)y, 100.0F + Minecraft.getInstance().getItemRenderer().zLevel);
        matrix.translate(8.0F, 8.0F, 0.0F);
        matrix.scale(1.0F, -1.0F, 1.0F);
        matrix.scale(16.0F, 16.0F, 16.0F);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IBakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(stack, null, null);
        boolean flag = !model.isSideLit();
        if (flag) {
            RenderHelper.setupGuiFlatDiffuseLighting();
        }
        RenderSystem.disableDepthTest();
        Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GUI, false, matrix, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, model);
        irendertypebuffer$impl.finish();
        RenderSystem.enableDepthTest();
        if (flag) {
            RenderHelper.setupGui3DDiffuseLighting();
        }
        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        matrix.pop();
    }

    @SuppressWarnings("deprecation")
    public static void renderItemOverlayIntoGUI(MatrixStack matrix, FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
        if (!stack.isEmpty()) {
            if (stack.getCount() != 1 || text != null) {
                matrix.push();
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                matrix.translate(0.0D, 0.0D, Minecraft.getInstance().getItemRenderer().zLevel + 200.0F);
                IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
                fr.renderString(s, (float)(xPosition + 19 - 2 - fr.getStringWidth(s)), (float)(yPosition + 6 + 3), 16777215, true, matrix.getLast().getMatrix(), irendertypebuffer$impl, false, 0, 15728880);
                irendertypebuffer$impl.finish();
                RenderSystem.enableDepthTest();
                matrix.pop();
            }

            if (stack.getItem().showDurabilityBar(stack)) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                double health = stack.getItem().getDurabilityForDisplay(stack);
                int i = Math.round(13.0F - (float)health * 13.0F);
                int j = stack.getItem().getRGBDurabilityForDisplay(stack);
                draw(bufferbuilder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                draw(bufferbuilder, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;
            float f3 = clientplayerentity == null ? 0.0F : clientplayerentity.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getInstance().getRenderPartialTicks());
            if (f3 > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tessellator tessellator1 = Tessellator.getInstance();
                BufferBuilder bufferbuilder1 = tessellator1.getBuffer();
                draw(bufferbuilder1, xPosition, yPosition + MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
                RenderSystem.disableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }
    }

    private static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos((double)(x + 0), (double)(y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + 0), (double)(y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + width), (double)(y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + width), (double)(y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }

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
    }
}
