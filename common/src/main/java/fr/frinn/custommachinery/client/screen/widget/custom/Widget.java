package fr.frinn.custommachinery.client.screen.widget.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public abstract class Widget implements GuiEventListener {

    private final Supplier<Integer> x;
    private final Supplier<Integer> y;
    public final int width;
    public final int height;

    public Widget(Supplier<Integer> x, Supplier<Integer> y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return this.x.get();
    }

    public int getY() {
        return this.y.get();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX <= getX() + this.width && mouseY >= getY() && mouseY <= getY() + this.height;
    }

    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {

    }

    public List<Component> getTooltips() {
        return Collections.emptyList();
    }

    public void playDownSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }
}
