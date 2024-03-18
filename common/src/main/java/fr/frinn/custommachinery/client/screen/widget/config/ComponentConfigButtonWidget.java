package fr.frinn.custommachinery.client.screen.widget.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

import java.util.function.Supplier;

public class ComponentConfigButtonWidget extends Button {

    public ComponentConfigButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, Supplier::get);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, FastColor.ARGB32.color(127, 0, 0, 255));
    }
}
