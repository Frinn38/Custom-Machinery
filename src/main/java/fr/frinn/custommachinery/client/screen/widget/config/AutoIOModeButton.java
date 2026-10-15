package fr.frinn.custommachinery.client.screen.widget.config;

import fr.frinn.custommachinery.client.screen.ScreenUtils;
import fr.frinn.custommachinery.impl.component.config.SideConfig.ConfigButtonData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.function.Supplier;

public class AutoIOModeButton extends ImageButton {

    private static final MutableComponent INPUT = Component.translatable("custommachinery.gui.config.auto_input");
    private static final MutableComponent OUTPUT = Component.translatable("custommachinery.gui.config.auto_output");
    private static final MutableComponent ENABLED = Component.translatable("custommachinery.gui.config.enabled").withStyle(ChatFormatting.GREEN);
    private static final MutableComponent DISABLED = Component.translatable("custommachinery.gui.config.disabled").withStyle(ChatFormatting.RED);

    private final Supplier<Boolean> enabled;
    private final boolean input;

    public AutoIOModeButton(int x, int y, Supplier<Boolean> enabled, boolean input, OnPress onPress, ConfigButtonData data) {
        super(x + data.x(), y + data.y(), data.width(), data.height(), ScreenUtils.dataToSprite(data.sprites()), onPress, input ? INPUT : OUTPUT);
        this.enabled = enabled;
        this.input = input;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int color = ARGB.color(255, 255, 255, 255);
        if(this.input && this.enabled.get())
            color = ARGB.color(255, 255, 85, 85);
        else if(!this.input && this.enabled.get())
            color = ARGB.color(255, 85, 85, 255);
        Identifier sprite = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.width, this.height, color);
        this.updateTooltips();
    }

    private void updateTooltips() {
        MutableComponent tooltip = (this.input ? INPUT : OUTPUT).copy();
        tooltip.append("\n");
        if(this.enabled.get())
            tooltip.append(ENABLED);
        else
            tooltip.append(DISABLED);
        this.setTooltip(Tooltip.create(tooltip));
    }
}
