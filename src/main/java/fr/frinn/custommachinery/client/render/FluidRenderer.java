package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

public class FluidRenderer {

    private static final int MIN_FLUID_SIZE = 1;
    private static final int TEXTURE_SIZE = 16;

    public static void renderFluid(PoseStack poseStack, int posX, int posY, int width, int height, FluidStack fluidStack, int capacity) {
        renderFluid(poseStack, posX, posY, width, height, fluidStack, capacity, Orientation.RIGHT);
    }

    public static void renderFluid(PoseStack poseStack, int posX, int posY, int width, int height, FluidStack fluidStack, int capacity, Orientation orientation) {
        Fluid fluid = fluidStack.getFluid();
        if (fluid == Fluids.EMPTY || fluidStack.isEmpty() || capacity == 0)
            return;

        RenderSystem.enableBlend();

        poseStack.pushPose();
        poseStack.translate(posX, posY, 0);

        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extensions.getStillTexture(fluidStack));
        int fluidColor = extensions.getTintColor(fluidStack);

        int amount = fluidStack.getAmount();
        double fillingPercent = (double) amount / capacity;

        int xOffset = 0;
        int fluidWidth = width;
        int yOffset = 0;
        int fluidHeight = height;

        switch (orientation) {
            case TOP -> {
                fluidHeight = (int) (fillingPercent * height);
                if(amount > 0 && height < MIN_FLUID_SIZE)
                    fluidHeight = MIN_FLUID_SIZE;
                yOffset = height - fluidHeight;
            }
            case BOTTOM -> {
                fluidHeight = (int) (fillingPercent * height);
                if(amount > 0 && height < MIN_FLUID_SIZE)
                    fluidHeight = MIN_FLUID_SIZE;
            }
            case RIGHT -> {
                fluidWidth = (int) (fillingPercent * width);
                if(amount > 0 && width < MIN_FLUID_SIZE)
                    fluidWidth = MIN_FLUID_SIZE;
            }
            case LEFT -> {
                fluidWidth = (int) (fillingPercent * width);
                if(amount > 0 && width < MIN_FLUID_SIZE)
                    fluidWidth = MIN_FLUID_SIZE;
                xOffset = width - fluidWidth;
            }
        }

        drawTiledSprite(poseStack, xOffset, fluidWidth, yOffset, fluidHeight, fluidColor, sprite);

        poseStack.popPose();

        RenderSystem.disableBlend();
    }

    private static void drawTiledSprite(PoseStack poseStack, int xOffset, int tiledWidth, int yOffset, int tiledHeight, int color, TextureAtlasSprite sprite) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        Matrix4f matrix = poseStack.last().pose();
        setGLColorFromInt(color);

        final int xTileCount = tiledWidth / TEXTURE_SIZE;
        final int xRemainder = tiledWidth - (xTileCount * TEXTURE_SIZE);
        final int yTileCount = tiledHeight / TEXTURE_SIZE;
        final int yRemainder = tiledHeight - (yTileCount * TEXTURE_SIZE);

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int width = (xTile == xTileCount) ? xRemainder : TEXTURE_SIZE;
                int height = (yTile == yTileCount) ? yRemainder : TEXTURE_SIZE;
                int x = xOffset + (xTile * TEXTURE_SIZE);
                int y = yOffset + tiledHeight - ((yTile + 1) * TEXTURE_SIZE);
                if (width > 0 && height > 0) {
                    int maskTop = TEXTURE_SIZE - height;
                    int maskRight = TEXTURE_SIZE - width;

                    drawTextureWithMasking(matrix, x, y, sprite, maskTop, maskRight);
                }
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void setGLColorFromInt(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alpha = ((color >> 24) & 0xFF) / 255F;
        if(alpha == 0)
            alpha = 1;
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    private static void drawTextureWithMasking(Matrix4f matrix, float xCord, float yCord, TextureAtlasSprite textureSprite, int maskTop, int maskRight) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - (maskRight / 16F * (uMax - uMin));
        vMax = vMax - (maskTop / 16F * (vMax - vMin));

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix, xCord, yCord + 16, 100F).setUv(uMin, vMax);
        bufferBuilder.addVertex(matrix, xCord + 16 - maskRight, yCord + 16, 100F).setUv(uMax, vMax);
        bufferBuilder.addVertex(matrix, xCord + 16 - maskRight, yCord + maskTop, 100F).setUv(uMax, vMin);
        bufferBuilder.addVertex(matrix, xCord, yCord + maskTop, 100F).setUv(uMin, vMin);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }
}
