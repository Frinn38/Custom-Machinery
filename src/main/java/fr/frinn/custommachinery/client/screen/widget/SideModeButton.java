package fr.frinn.custommachinery.client.screen.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.apiimpl.component.config.RelativeSide;
import fr.frinn.custommachinery.apiimpl.component.config.SideConfig;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.ComponentConfigScreen;
import fr.frinn.custommachinery.common.network.CChangeSideModePacket;
import fr.frinn.custommachinery.common.network.NetworkManager;
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
        if(isHoveredOrFocused())
            blit(pose, x, y, 0, 14, width, height, width, 28);
        else
            blit(pose, x, y, 0, 0, width, height, width, 28);
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
                    NetworkManager.CHANNEL.sendToServer(new CChangeSideModePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId(), (byte)this.side.ordinal(), true));
                else
                    NetworkManager.CHANNEL.sendToServer(new CChangeSideModePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId(), (byte)this.side.ordinal(), false));
                return true;
        }
        return false;
    }

    private String getComponentId() {
        return this.config.getComponent().getType().getRegistryName().toString() + ":" + this.config.getComponent().getId();
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
