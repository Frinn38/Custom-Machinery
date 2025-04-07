package fr.frinn.custommachinery.client.screen.widget.config;

import fr.frinn.custommachinery.common.util.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class ComponentConfigButton extends Button {

    private final Color color;

    public ComponentConfigButton(int x, int y, int width, int height, Component message, OnPress onPress, Color color) {
        super(x, y, width, height, message, onPress, Supplier::get);
        if(color.getAlpha() == 0)
            this.color = Color.fromColors(127, color.getRed(), color.getGreen(), color.getBlue());
        else
            this.color = color;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.color.getARGB());
    }
}
