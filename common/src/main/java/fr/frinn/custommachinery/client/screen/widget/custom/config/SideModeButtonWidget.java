package fr.frinn.custommachinery.client.screen.widget.custom.config;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.widget.custom.Widget;
import fr.frinn.custommachinery.common.network.CChangeSideModePacket;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.List;
import java.util.function.Supplier;

public class SideModeButtonWidget extends Widget {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/side_mode.png");

    private final SideConfig config;
    private final RelativeSide side;
    private final List<Component> tooltips;

    public SideModeButtonWidget(Supplier<Integer> x, Supplier<Integer> y, SideConfig config, RelativeSide side) {
        super(x, y, 14, 14);
        this.config = config;
        this.side = side;
        this.tooltips = Lists.newArrayList(this.side.getTranslationName(), this.config.getSideMode(this.side).title());
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        ClientHandler.bindTexture(TEXTURE);
        int color = this.config.getSideMode(this.side).color();
        float r = FastColor.ARGB32.red(color) / 255.0F;
        float g = FastColor.ARGB32.green(color) / 255.0F;
        float b = FastColor.ARGB32.blue(color) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, 1);
        if(this.isMouseOver(mouseX, mouseY))
            GuiComponent.blit(pose, this.getX(), this.getY(), 0, 14, this.width, this.height, this.width, 28);
        else
            GuiComponent.blit(pose, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, 28);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    public List<Component> getTooltips() {
        return this.tooltips;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        playDownSound();
        if (Minecraft.getInstance().player == null)
            return true;
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
