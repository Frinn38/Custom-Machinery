package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.TextGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class TextGuiElementWidget extends AbstractGuiElementWidget<TextGuiElement> {

    public TextGuiElementWidget(TextGuiElement element, IMachineScreen screen) {
        super(element, screen, element.getText());
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        int posX = switch (this.getElement().getAlignment()) {
            case CENTER -> this.x - Minecraft.getInstance().font.width(this.getElement().getText().getString()) / 2;
            case RIGHT -> this.x - Minecraft.getInstance().font.width(this.getElement().getText().getString());
            default -> this.x;
        };
        int posY = this.y;

        pose.pushPose();
        float scaleX = 1.0F;
        float scaleY = 1.0F;
        if(this.getElement().getWidth() >= 0)
            scaleX = (float)this.getElement().getWidth() / (float)Minecraft.getInstance().font.width(this.getElement().getText());

        if(this.getElement().getHeight() >= 0)
            scaleY = (float)this.getElement().getHeight() / (float)Minecraft.getInstance().font.lineHeight;

        if(scaleX == 1.0F && scaleY != 1.0F)
            scaleX = scaleY;
        else if(scaleX != 1.0F && scaleY == 1.0F)
            scaleY = scaleX;

        if(scaleX != 1.0F) {
            pose.translate(this.x, this.y, 0);
            pose.scale(scaleX, scaleY, 1.0F);
            pose.translate(-this.x, -this.y, 0);
        }
        Minecraft.getInstance().font.draw(pose, this.getElement().getText(), posX, posY, 0);
        pose.popPose();
    }
}
