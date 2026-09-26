package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.frinn.custommachinery.client.CMRenderPipelines;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.util.Mth;
import org.joml.Math;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;

public class ProgressArrowRenderer {

    public static void renderProgressArrow(GuiGraphicsExtractor graphics, ProgressBarGuiElement element, int x, int y, double progress) {
        //Radial
        if(element.getDirection() == Orientation.CLOCKWISE || element.getDirection() == Orientation.COUNTER_CLOCKWISE) {
            renderRadialProgress(graphics, element, x, y, progress);
            return;
        }

        int width = element.getWidth();
        int height = element.getHeight();

        int filledWidth = (int)(width * Mth.clamp(Mth.map(progress, element.getStart(), element.getEnd(), 0, 1), 0.0D, 1.0D));
        int filledHeight = (int)(height * Mth.clamp(Mth.map(progress, element.getStart(), element.getEnd(), 0, 1), 0.0D, 1.0D));

        if(element.getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && element.getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE)) {
            graphics.pose().pushMatrix();
            if(element.getDirection() == Orientation.TOP || element.getDirection() == Orientation.BOTTOM) {
                width = element.getHeight();
                height = element.getWidth();
                filledWidth = (int)(width * Mth.clamp(Mth.map(progress, element.getStart(), element.getEnd(), 0, 1), 0.0D, 1.0D));
            }
            rotate(graphics.pose(), element.getDirection(), x, y, width, height);

            ClientHandler.blit(graphics, element.getEmptyTexture(), 0, 0, width, height);
            graphics.blit(element.getFilledTexture().texture(), 0, 0, element.getFilledTexture().u(), element.getFilledTexture().v(), filledWidth, height, width, height);

            graphics.pose().popMatrix();
        } else {
            ClientHandler.blit(graphics, element.getEmptyTexture(), x, y, width, height);
            TextureInfo filled = element.getFilledTexture();
            switch (element.getDirection()) {
                case RIGHT -> graphics.blit(filled.texture(), x, y, filled.u(), filled.v(), filledWidth, height, width, height);
                case LEFT -> graphics.blit(filled.texture(), x + width - filledWidth, y, filled.u() + width - filledWidth, filled.v(), filledWidth, height, width, height);
                case BOTTOM -> graphics.blit(filled.texture(), x, y, filled.u(), filled.v(), width, filledHeight, width, height);
                case TOP -> graphics.blit(filled.texture(), x, y + height - filledHeight, filled.u(), filled.v() + height - filledHeight, width, filledHeight, width, height);
            }
        }
    }

    public static void rotate(Matrix3x2fStack matrix, ProgressBarGuiElement.Orientation orientation, int posX, int posY, int width, int height) {
        switch (orientation) {
            case RIGHT -> matrix.translate(posX, posY);
            case LEFT -> {
                matrix.rotate(Math.toRadians(180));
                matrix.translate(-width - posX, -height - posY);
            }
            case TOP -> {
                matrix.rotate(Math.toRadians(270));
                matrix.translate(-width - posY, posX);
            }
            case BOTTOM -> {
                matrix.rotate(Math.toRadians(90));
                matrix.translate(posY, -height - posX);
            }
        }
    }

    public static void renderRadialProgress(GuiGraphicsExtractor graphics, ProgressBarGuiElement element, int x, int y, double progress) {
        int width = element.getWidth();
        int height = element.getHeight();

        ClientHandler.blit(graphics, element.getEmptyTexture(), x, y, width, height);
        TextureInfo filled = element.getFilledTexture();

        /*
        RenderSystem.setShaderTexture(0, filled.texture());
        RenderSystem.setShader(() -> ClientHandler.RADIAL_FILL_SHADER);
        ClientHandler.RADIAL_FILL_SHADER.safeGetUniform("Progress").set((float)progress);
        if(element.getDirection() == Orientation.COUNTER_CLOCKWISE)
            ClientHandler.RADIAL_FILL_SHADER.safeGetUniform("Reverse").set(1.0F);

         */
        
        graphics.blit(CMRenderPipelines.GUI_RADIAL_FILL, filled.texture(), x, y, filled.u(), filled.v(), width, height, filled.width(), filled.height(), 0);
    }
}
