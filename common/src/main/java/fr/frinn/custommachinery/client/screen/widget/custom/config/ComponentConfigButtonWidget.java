package fr.frinn.custommachinery.client.screen.widget.custom.config;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.client.screen.widget.custom.ButtonWidget;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.FastColor;

import java.util.function.Supplier;

public class ComponentConfigButtonWidget extends ButtonWidget {

    public ComponentConfigButtonWidget(Supplier<Integer> x, Supplier<Integer> y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        GuiComponent.fill(pose, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, FastColor.ARGB32.color(127, 0, 0, 255));
    }
}
