package fr.frinn.custommachinery.client.screen.widget.config;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.network.CChangeSideModePacket;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class SideModeButtonWidget extends AbstractWidget {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/side_mode.png");

    private final SideConfig config;
    private final RelativeSide side;

    public SideModeButtonWidget(int x, int y, SideConfig config, RelativeSide side) {
        super(x, y, 14, 14, side.getTranslationName());
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
        if(this.isMouseOver(mouseX, mouseY))
            graphics.blit(TEXTURE, this.getX(), this.getY(), 0, 14, this.width, this.height, this.width, 28);
        else
            graphics.blit(TEXTURE, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, 28);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!this.clicked(mouseX, mouseY) || Minecraft.getInstance().player == null)
            return false;
        playDownSound(Minecraft.getInstance().getSoundManager());
        if (button == 0)
            new CChangeSideModePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId(), (byte) this.side.ordinal(), true).sendToServer();
        else
            new CChangeSideModePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId(), (byte) this.side.ordinal(), false).sendToServer();
        return true;
    }

    private String getComponentId() {
        return this.config.getComponent().getType().getId().toString() + ":" + this.config.getComponent().getId();
    }
}
