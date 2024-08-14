package fr.frinn.custommachinery.client.screen.widget.config;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.network.CChangeSideModePacket;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
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

public class SideModeButton extends ImageButton {

    private static final WidgetSprites SPRITES = new WidgetSprites(CustomMachinery.rl("config/side_mode_button"), CustomMachinery.rl("config/side_mode_button_hovered"));

    private final SideConfig config;
    private final RelativeSide side;

    public SideModeButton(int x, int y, SideConfig config, RelativeSide side) {
        super(x, y, 14, 14, SPRITES, button -> {}, side.getTranslationName());
        this.config = config;
        this.side = side;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int color = this.config.getSideMode(this.side).color();
        float r = FastColor.ARGB32.red(color) / 255.0F;
        float g = FastColor.ARGB32.green(color) / 255.0F;
        float b = FastColor.ARGB32.blue(color) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, 1);
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        this.updateTooltips();
    }

    private void updateTooltips() {
        MutableComponent tooltip = Component.empty();
        tooltip.append(this.side.getTranslationName());
        tooltip.append("\n");
        tooltip.append(this.config.getSideMode(this.side).title());
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!this.clicked(mouseX, mouseY) || Minecraft.getInstance().player == null)
            return false;
        playDownSound(Minecraft.getInstance().getSoundManager());
        if (button == 0)
            PacketDistributor.sendToServer(new CChangeSideModePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId(), (byte) this.side.ordinal(), true));
        else
            PacketDistributor.sendToServer(new CChangeSideModePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId(), (byte) this.side.ordinal(), false));
        return true;
    }

    private String getComponentId() {
        return this.config.getComponent().getType().getId().toString() + ":" + this.config.getComponent().getId();
    }
}
