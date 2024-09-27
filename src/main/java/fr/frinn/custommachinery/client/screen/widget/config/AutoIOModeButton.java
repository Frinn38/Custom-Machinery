package fr.frinn.custommachinery.client.screen.widget.config;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;

import java.util.function.Supplier;

public class AutoIOModeButton extends ImageButton {

    private static final WidgetSprites SPRITES = new WidgetSprites(CustomMachinery.rl("config/auto_io_button"), CustomMachinery.rl("config/auto_io_button_hovered"));
    private static final MutableComponent INPUT = Component.translatable("custommachinery.gui.config.auto_input");
    private static final MutableComponent OUTPUT = Component.translatable("custommachinery.gui.config.auto_output");
    private static final MutableComponent ENABLED = Component.translatable("custommachinery.gui.config.enabled").withStyle(ChatFormatting.GREEN);
    private static final MutableComponent DISABLED = Component.translatable("custommachinery.gui.config.disabled").withStyle(ChatFormatting.RED);

    private final Supplier<Boolean> enabled;
    private final boolean input;

    public AutoIOModeButton(int x, int y, Supplier<Boolean> enabled, boolean input, OnPress onPress) {
        super(x, y, 28, 14, SPRITES, onPress, input ? INPUT : OUTPUT);
        this.enabled = enabled;
        this.input = input;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int color = FastColor.ARGB32.color(255, 255, 255, 255);
        if(this.input && this.enabled.get())
            color = FastColor.ARGB32.color(255, 255, 85, 85);
        else if(!this.input && this.enabled.get())
            color = FastColor.ARGB32.color(255, 85, 85, 255);
        float r = FastColor.ARGB32.red(color) / 255.0F;
        float g = FastColor.ARGB32.green(color) / 255.0F;
        float b = FastColor.ARGB32.blue(color) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, 1);
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        this.updateTooltip();
    }

    private void updateTooltip() {
        MutableComponent tooltip = (this.input ? INPUT : OUTPUT).copy();
        tooltip.append("\n");
        if(this.enabled.get())
            tooltip.append(ENABLED);
        else
            tooltip.append(DISABLED);
        this.setTooltip(Tooltip.create(tooltip));
    }
}
