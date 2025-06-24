package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ProgressArrowRenderer {

    public static void renderProgressArrow(GuiGraphics graphics, ProgressBarGuiElement element, int x, int y, double progress) {
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
}
