package fr.frinn.custommachinery.client.screen.widget.config;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.network.CChangeSideModePacket;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class AutoIOModeButton extends ImageButton {

    private static final WidgetSprites SPRITES = new WidgetSprites(CustomMachinery.rl("config/auto_io_button"), CustomMachinery.rl("config/auto_io_button_hovered"));
    private static final MutableComponent INPUT = Component.translatable("custommachinery.gui.config.auto_input");
    private static final MutableComponent OUTPUT = Component.translatable("custommachinery.gui.config.auto_output");
    private static final MutableComponent ENABLED = Component.translatable("custommachinery.gui.config.enabled").withStyle(ChatFormatting.GREEN);
    private static final MutableComponent DISABLED = Component.translatable("custommachinery.gui.config.disabled").withStyle(ChatFormatting.RED);

    private final SideConfig config;
    private final boolean input;

    public AutoIOModeButton(int x, int y, SideConfig config, boolean input) {
        super(x, y, 28, 14, SPRITES, button -> {}, input ? INPUT : OUTPUT);
        this.config = config;
        this.input = input;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int color = FastColor.ARGB32.color(255, 255, 255, 255);
        if(this.input && this.config.isAutoInput())
            color = FastColor.ARGB32.color(255, 255, 85, 85);
        else if(!this.input && this.config.isAutoOutput())
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
        if((this.input && this.config.isAutoInput()) || (!this.input && this.config.isAutoOutput()))
            tooltip.append(ENABLED);
        else
            tooltip.append(DISABLED);
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!this.clicked(mouseX, mouseY) || (button != 0 && button != 1) || Minecraft.getInstance().player == null)
            return false;
        playDownSound(Minecraft.getInstance().getSoundManager());
        PacketDistributor.sendToServer(new CChangeSideModePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId(), (byte) (this.input ? 6 : 7), true));
        return true;
    }

    private String getComponentId() {
        return this.config.getComponent().getType().getId().toString() + ":" + this.config.getComponent().getId();
    }
}
