package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.TextGuiElement;
import fr.frinn.custommachinery.impl.client.AbstractGuiElementRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class TextGuiElementWidget extends AbstractGuiElementRenderable<TextGuiElement> {

    public TextGuiElementWidget(TextGuiElement element, IMachineScreen screen) {
        super(element, screen);
    }

    @Override
    public void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int posX = switch (this.getElement().getAlignment()) {
            case CENTER -> this.getElement().getX() - Minecraft.getInstance().font.width(this.getElement().getText().getString()) / 2;
            case RIGHT -> this.getElement().getX() - Minecraft.getInstance().font.width(this.getElement().getText().getString());
            default -> this.getElement().getX();
        };
        int posY = this.getElement().getY();

        graphics.pose().pushMatrix();
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
            graphics.pose().translation(this.getElement().getX(), this.getElement().getY());
            graphics.pose().scale(scaleX, scaleY);
            graphics.pose().translation(-this.getElement().getX(), -this.getElement().getY());
        }
        graphics.text(Minecraft.getInstance().font, this.getElement().getText(), posX, posY, 0, false);
        graphics.pose().popMatrix();
    }
}
