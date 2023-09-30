package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.guielement.ProgressBarGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ProgressGuiElementWidget extends AbstractGuiElementWidget<ProgressBarGuiElement> {

    public ProgressGuiElementWidget(ProgressBarGuiElement element, IMachineScreen screen) {
        super(element, screen, Component.literal("Progress Bar"));
    }

    @Override
    public void renderButton(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        int filledWidth = (int)(this.width * Mth.clamp(Mth.map(getRecipeProgressPercent(), this.getElement().getStart(), this.getElement().getEnd(), 0, 1), 0.0D, 1.0D));
        int filledHeight = (int)(this.height * Mth.clamp(Mth.map(getRecipeProgressPercent(), this.getElement().getStart(), this.getElement().getEnd(), 0, 1), 0.0D, 1.0D));

        ClientHandler.bindTexture(this.getElement().getEmptyTexture());

        if(this.getElement().getEmptyTexture().equals(ProgressBarGuiElement.BASE_EMPTY_TEXTURE) && this.getElement().getFilledTexture().equals(ProgressBarGuiElement.BASE_FILLED_TEXTURE)) {
            matrix.pushPose();
            rotate(matrix, this.getElement().getDirection(), this.x, this.y, this.width, this.height);

            GuiComponent.blit(matrix, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            ClientHandler.bindTexture(this.getElement().getFilledTexture());
            GuiComponent.blit(matrix, 0, 0, 0, 0, filledWidth, this.height, this.width, this.height);

            matrix.popPose();
        } else {
            GuiComponent.blit(matrix, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
            ClientHandler.bindTexture(this.getElement().getFilledTexture());
            switch (this.getElement().getDirection()) {
                case RIGHT -> GuiComponent.blit(matrix, this.x, this.y, 0, 0, filledWidth, this.height, this.width, this.height);
                case LEFT -> GuiComponent.blit(matrix, this.x + width - filledWidth, this.y, this.width - filledWidth, 0, filledWidth, this.height, this.width, this.height);
                case TOP -> GuiComponent.blit(matrix, this.x, this.y, 0, 0, this.width, filledHeight, this.width, this.height);
                case BOTTOM -> GuiComponent.blit(matrix, this.x, this.y + this.height - filledHeight, 0, this.height - filledHeight, this.width, filledHeight, this.width, this.height);
            }
        }
    }

    public double getRecipeProgressPercent() {
        if(this.getScreen().getTile().getProcessor() instanceof MachineProcessor machineProcessor && machineProcessor.getRecipeTotalTime() > 0)
            return machineProcessor.getRecipeProgressTime() / (double) machineProcessor.getRecipeTotalTime();
        return 0;
    }

    public static void rotate(PoseStack matrix, ProgressBarGuiElement.Orientation orientation, int posX, int posY, int width, int height) {
        switch (orientation) {
            case RIGHT -> matrix.translate(posX, posY, 0);
            case LEFT -> {
                matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
                matrix.translate(-width - posX, -height - posY, 0);
            }
            case TOP -> {
                matrix.mulPose(Vector3f.ZP.rotationDegrees(270));
                matrix.translate(-width - posY, posX, 0);
            }
            case BOTTOM -> {
                matrix.mulPose(Vector3f.ZP.rotationDegrees(90));
                matrix.translate(posY, -height - posX, 0);
            }
        }
    }
}
