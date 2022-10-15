package fr.frinn.custommachinery.client.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

public class ComponentConfigButton extends Button {

    public ComponentConfigButton(int x, int y, int width, int height, Component message, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, width, height, message, onPress, onTooltip);
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        fill(pose, x, y, x + width, y + height, FastColor.ARGB32.color(127, 0, 0, 255));
    }
}
