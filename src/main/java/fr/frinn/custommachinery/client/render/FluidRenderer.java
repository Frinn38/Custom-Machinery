package fr.frinn.custommachinery.client.render;

import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidRenderer {

    private static final int MIN_FLUID_SIZE = 1;

    public static void renderFluid(GuiGraphicsExtractor graphics, int posX, int posY, int width, int height, FluidStack fluidStack, int capacity) {
        renderFluid(graphics, posX, posY, width, height, fluidStack, capacity, Orientation.RIGHT);
    }

    public static void renderFluid(GuiGraphicsExtractor graphics, int posX, int posY, int width, int height, FluidStack fluidStack, int capacity, Orientation orientation) {
        Fluid fluid = fluidStack.getFluid();
        if (fluid == Fluids.EMPTY || fluidStack.isEmpty() || capacity == 0)
            return;

        graphics.pose().pushMatrix();
        graphics.pose().translate(posX, posY);

        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        TextureAtlasSprite sprite = model.stillMaterial().sprite();
        int fluidColor = model.fluidTintSource() == null ? ARGB.color(255, 255, 255, 255) : model.fluidTintSource().colorAsStack(fluidStack);

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

        drawTiledSprite(graphics, posX, posY, xOffset, yOffset, fluidWidth, fluidHeight, sprite, fluidColor);

        graphics.pose().popMatrix();
    }

    public static void drawTiledSprite(GuiGraphicsExtractor guiGraphics, int xPosition, int yPosition, int xOffset, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite, int color) {
        if (desiredWidth == 0 || desiredHeight == 0)
            return;

        SpriteContents spriteContents = sprite.contents();
        int xStart = xPosition + xOffset - desiredWidth;
        int yStart = yPosition + yOffset - desiredHeight;
        guiGraphics.enableScissor(xStart, yStart, xStart + desiredWidth, yStart + desiredHeight);
            guiGraphics.blitTiledSprite(
                    RenderPipelines.GUI_TEXTURED,
                    sprite,
                    xPosition,
                    yStart,
                    desiredWidth,
                    desiredHeight,
                    0,
                    0,
                    spriteContents.width(),
                    spriteContents.height(),
                    spriteContents.width(),
                    spriteContents.height(),
                    color
            );
        guiGraphics.disableScissor();
    }
}
