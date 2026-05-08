package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ProgressArrowRenderer {

    public static void renderProgressArrow(GuiGraphics graphics, ProgressBarGuiElement element, int x, int y, double progress) {
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
            graphics.pose().pushPose();
            if(element.getDirection() == Orientation.TOP || element.getDirection() == Orientation.BOTTOM) {
                width = element.getHeight();
                height = element.getWidth();
                filledWidth = (int)(width * Mth.clamp(Mth.map(progress, element.getStart(), element.getEnd(), 0, 1), 0.0D, 1.0D));
            }
            rotate(graphics.pose(), element.getDirection(), x, y, width, height);

            ClientHandler.blit(graphics, element.getEmptyTexture(), 0, 0, width, height);
            graphics.blit(element.getFilledTexture().texture(), 0, 0, element.getFilledTexture().u(), element.getFilledTexture().v(), filledWidth, height, width, height);

            graphics.pose().popPose();
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

    public static void rotate(PoseStack matrix, ProgressBarGuiElement.Orientation orientation, int posX, int posY, int width, int height) {
        switch (orientation) {
            case RIGHT -> matrix.translate(posX, posY, 0);
            case LEFT -> {
                matrix.mulPose(new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 0, 1.0f), 180));
                matrix.translate(-width - posX, -height - posY, 0);
            }
            case TOP -> {
                matrix.mulPose(new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 0, 1.0f), 270));
                matrix.translate(-width - posY, posX, 0);
            }
            case BOTTOM -> {
                matrix.mulPose(new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 0, 1.0f), 90));
                matrix.translate(posY, -height - posX, 0);
            }
        }
    }

    public static void renderRadialProgress(GuiGraphics graphics, ProgressBarGuiElement element, int x, int y, double progress) {
        int width = element.getWidth();
        int height = element.getHeight();

        ClientHandler.blit(graphics, element.getEmptyTexture(), x, y, width, height);
        TextureInfo filled = element.getFilledTexture();

        RenderSystem.setShaderTexture(0, filled.texture());
        RenderSystem.setShader(() -> ClientHandler.RADIAL_FILL_SHADER);
        ClientHandler.RADIAL_FILL_SHADER.safeGetUniform("Progress").set((float)progress);
        if(element.getDirection() == Orientation.COUNTER_CLOCKWISE)
            ClientHandler.RADIAL_FILL_SHADER.safeGetUniform("Reverse").set(1.0F);
        Matrix4f matrix4f = graphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix4f, (float)x, (float)y, 0).setUv(filled.u() / (float)width, filled.v() / (float)height);
        bufferbuilder.addVertex(matrix4f, (float)x, (float)y + height, 0).setUv(filled.u() / (float)width, (filled.v() + (float)height) / (float)height);
        bufferbuilder.addVertex(matrix4f, (float)x + width, (float)y + height, 0).setUv((filled.u() + (float)width) / (float)width, (filled.v() + (float)height) / (float)height);
        bufferbuilder.addVertex(matrix4f, (float)x + width, (float)y, 0).setUv((filled.u() + (float)width) / (float)width, filled.v() / (float)height);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }
}
