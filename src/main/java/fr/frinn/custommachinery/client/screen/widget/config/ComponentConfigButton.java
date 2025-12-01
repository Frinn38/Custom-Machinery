package fr.frinn.custommachinery.client.screen.widget.config;

import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.popup.ComponentConfigPopup;
import fr.frinn.custommachinery.common.util.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class ComponentConfigButton extends Button {

    private final Color color;
    private final ComponentConfigPopup popup;

    public ComponentConfigButton(int x, int y, int width, int height, Component message, OnPress onPress, Color color, ComponentConfigPopup popup) {
        super(x, y, width, height, message, onPress, Supplier::get);
        if(color.getAlpha() == 0)
            this.color = Color.fromColors(127, color.getRed(), color.getGreen(), color.getBlue());
        else
            this.color = color;
        this.popup = popup;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.color.getARGB());
        if(this.popup.parent.popups().getFirst() == this.popup) {
            int offset = (int)(System.currentTimeMillis() / 100 % 100);
            ClientHandler.drawDottedRect(graphics, this.getX() - 1, this.getY() - 1, this.width + 1, this.height + 1, this.color.mul(0.5).getARGB(), 4, 2, offset);
        }
    }
}
