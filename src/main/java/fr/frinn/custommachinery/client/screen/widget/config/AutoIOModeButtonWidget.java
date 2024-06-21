package fr.frinn.custommachinery.client.screen.widget.config;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.network.CChangeSideModePacket;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class AutoIOModeButtonWidget extends AbstractWidget {

    private static final ResourceLocation TEXTURE = CustomMachinery.rl("textures/gui/config/auto_io_mode.png");
    private static final Component INPUT = Component.translatable("custommachinery.gui.config.auto_input");
    private static final Component OUTPUT = Component.translatable("custommachinery.gui.config.auto_output");
    private static final Component ENABLED = Component.translatable("custommachinery.gui.config.enabled").withStyle(ChatFormatting.GREEN);
    private static final Component DISABLED = Component.translatable("custommachinery.gui.config.disabled").withStyle(ChatFormatting.RED);

    private final SideConfig config;
    private final boolean input;

    public AutoIOModeButtonWidget(int x, int y, SideConfig config, boolean input) {
        super(x, y, 28, 14, input ? INPUT : OUTPUT);
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
        if(this.isHovered())
            graphics.blit(TEXTURE, this.getX(), this.getY(), 0, 14, this.width, this.height, this.width, 28);
        else
            graphics.blit(TEXTURE, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, 28);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public List<Component> getTooltips() {
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(this.input ? INPUT : OUTPUT);
        if((this.input && this.config.isAutoInput()) || (!this.input && this.config.isAutoOutput()))
            tooltips.add(ENABLED);
        else
            tooltips.add(DISABLED);
        return tooltips;
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
