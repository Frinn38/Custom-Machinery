package fr.frinn.custommachinery.client.screen.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.config.ComponentConfigScreen;
import fr.frinn.custommachinery.common.network.CChangeSideModePacket;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.List;

public class SideModeButton extends AbstractWidget {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/side_mode.png");

    private final ComponentConfigScreen parent;
    private final SideConfig config;
    private final RelativeSide side;

    public SideModeButton(ComponentConfigScreen parent, int x, int y, SideConfig config, RelativeSide side) {
        super(x, y, 14, 14, side.getTranslationName());
        this.parent = parent;
        this.config = config;
        this.side = side;
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        ClientHandler.bindTexture(TEXTURE);
        int color = this.config.getSideMode(this.side).color();
        float r = FastColor.ARGB32.red(color) / 255.0F;
        float g = FastColor.ARGB32.green(color) / 255.0F;
        float b = FastColor.ARGB32.blue(color) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, 1);
        this.isHovered = this.clicked(mouseX, mouseY);
        if(isHoveredOrFocused())
            blit(pose, this.x + this.parent.getOffsetX(), this.y + this.parent.getOffsetY(), 0, 14, this.width, this.height, this.width, 28);
        else
            blit(pose, this.x + this.parent.getOffsetX(), this.y + this.parent.getOffsetY(), 0, 0, this.width, this.height, this.width, 28);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    public void renderToolTip(PoseStack pose, int mouseX, int mouseY) {
        List<Component> tooltips = Lists.newArrayList(this.side.getTranslationName(), this.config.getSideMode(this.side).title());
        this.parent.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && this.isValidClickButton(button) && this.clicked(mouseX, mouseY)) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                if(Minecraft.getInstance().player == null)
                    return true;
                if(button == 0)
                    new CChangeSideModePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId(), (byte)this.side.ordinal(), true).sendToServer();
                else
                    new CChangeSideModePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId(), (byte)this.side.ordinal(), false).sendToServer();
                return true;
        }
        return false;
    }

    @Override
    protected boolean clicked(double d, double e) {
        return this.active
                && this.visible
                && d >= this.x + this.parent.offsetX
                && e >= this.y + this.parent.offsetY
                && d < this.x + this.parent.offsetX + this.width
                && e < this.y + this.parent.offsetY+ this.height;
    }

    private String getComponentId() {
        return this.config.getComponent().getType().getId().toString() + ":" + this.config.getComponent().getId();
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == 0 || button == 1;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
        output.add(NarratedElementType.HINT, this.config.getSideMode(this.side).title());
    }
}
