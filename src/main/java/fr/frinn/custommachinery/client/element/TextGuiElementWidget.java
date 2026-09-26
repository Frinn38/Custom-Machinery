package fr.frinn.custommachinery.client.element;

import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.TextGuiElement;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

public class TextGuiElementWidget extends AbstractGuiElementWidget<TextGuiElement> {

    public TextGuiElementWidget(TextGuiElement element, IMachineScreen screen) {
        super(element, screen, element.getText());
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int posX = switch (this.getElement().getAlignment()) {
            case CENTER -> this.getX() - Minecraft.getInstance().font.width(this.getElement().getText().getString()) / 2;
            case RIGHT -> this.getX() - Minecraft.getInstance().font.width(this.getElement().getText().getString());
            default -> this.getX();
        };
        int posY = this.getY();

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
            graphics.pose().translation(this.getX(), this.getY());
            graphics.pose().scale(scaleX, scaleY);
            graphics.pose().translation(-this.getX(), -this.getY());
        }
        graphics.text(Minecraft.getInstance().font, this.getElement().getText(), posX, posY, 0, false);
        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return false;
    }
}
