package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement.Orientation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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

            graphics.blit(element.getEmptyTexture(), 0, 0, 0, 0, width, height, width, height);
            graphics.blit(element.getFilledTexture(), 0, 0, 0, 0, filledWidth, height, width, height);

            graphics.pose().popPose();
        } else {
            graphics.blit(element.getEmptyTexture(), x, y, 0, 0, width, height, width, height);
            ResourceLocation filled = element.getFilledTexture();
            switch (element.getDirection()) {
                case RIGHT -> graphics.blit(filled, x, y, 0, 0, filledWidth, height, width, height);
                case LEFT -> graphics.blit(filled, x + width - filledWidth, y, width - filledWidth, 0, filledWidth, height, width, height);
                case BOTTOM -> graphics.blit(filled, x, y, 0, 0, width, filledHeight, width, height);
                case TOP -> graphics.blit(filled, x, y + height - filledHeight, 0, height - filledHeight, width, filledHeight, width, height);
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
