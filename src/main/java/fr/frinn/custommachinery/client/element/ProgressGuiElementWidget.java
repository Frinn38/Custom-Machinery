package fr.frinn.custommachinery.client.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ProgressGuiElementWidget extends AbstractGuiElementWidget<ProgressBarGuiElement> {

    public ProgressGuiElementWidget(ProgressBarGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Progress Bar"));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int filledWidth = (int)(this.width * Mth.clamp(Mth.map(getRecipeProgressPercent(), this.getElement().getStart(), this.getElement().getEnd(), 0, 1), 0.0D, 1.0D));
        int filledHeight = (int)(this.height * Mth.clamp(Mth.map(getRecipeProgressPercent(), this.getElement().getStart(), this.getElement().getEnd(), 0, 1), 0.0D, 1.0D));

        if(this.getElement().getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && this.getElement().getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE)) {
            graphics.pose().pushPose();
            rotate(graphics.pose(), this.getElement().getDirection(), this.getX(), this.getY(), this.width, this.height);

            graphics.blit(this.getElement().getEmptyTexture(), 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            graphics.blit(this.getElement().getFilledTexture(), 0, 0, 0, 0, filledWidth, this.height, this.width, this.height);

            graphics.pose().popPose();
        } else {
            graphics.blit(this.getElement().getEmptyTexture(), this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
            ResourceLocation filled = this.getElement().getFilledTexture();
            switch (this.getElement().getDirection()) {
                case RIGHT -> graphics.blit(filled, this.getX(), this.getY(), 0, 0, filledWidth, this.height, this.width, this.height);
                case LEFT -> graphics.blit(filled, this.getX() + width - filledWidth, this.getY(), this.width - filledWidth, 0, filledWidth, this.height, this.width, this.height);
                case TOP -> graphics.blit(filled, this.getX(), this.getY(), 0, 0, this.width, filledHeight, this.width, this.height);
                case BOTTOM -> graphics.blit(filled, this.getX(), this.getY() + this.height - filledHeight, 0, this.height - filledHeight, this.width, filledHeight, this.width, this.height);
            }
        }
    }

    public double getRecipeProgressPercent() {
        if(this.getScreen().getTile().getProcessor() instanceof MachineProcessor machineProcessor && machineProcessor.getRecipeTotalTime() > 0)
            return machineProcessor.getRecipeProgressTime() / (double) machineProcessor.getRecipeTotalTime();
        else if(this.getScreen().getTile().getMachine().isDummy())
            return (System.currentTimeMillis() % 2000) / 2000.0D;
        else
            return 0;
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

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return false;
    }
}
