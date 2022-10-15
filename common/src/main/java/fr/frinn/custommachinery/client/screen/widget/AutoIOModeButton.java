package fr.frinn.custommachinery.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.config.ComponentConfigScreen;
import fr.frinn.custommachinery.common.network.CChangeSideModePacket;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.ArrayList;
import java.util.List;

public class AutoIOModeButton extends AbstractWidget {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/config/auto_io_mode.png");
    private static final Component INPUT = new TranslatableComponent("custommachinery.gui.config.auto_input");
    private static final Component OUTPUT = new TranslatableComponent("custommachinery.gui.config.auto_output");
    private static final Component ENABLED = new TranslatableComponent("custommachinery.gui.config.enabled").withStyle(ChatFormatting.GREEN);
    private static final Component DISABLED = new TranslatableComponent("custommachinery.gui.config.disabled").withStyle(ChatFormatting.RED);

    private final ComponentConfigScreen parent;
    private final SideConfig config;
    private final boolean input;

    public AutoIOModeButton(ComponentConfigScreen parent, int x, int y, SideConfig config, boolean input) {
        super(x, y, 28, 14, input ? INPUT : OUTPUT);
        this.parent = parent;
        this.config = config;
        this.input = input;
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        ClientHandler.bindTexture(TEXTURE);
        int color = FastColor.ARGB32.color(255, 255, 255, 255);
        if(this.input && this.config.isAutoInput())
            color = FastColor.ARGB32.color(255, 255, 85, 85);
        else if(!this.input && this.config.isAutoOutput())
            color = FastColor.ARGB32.color(255, 85, 85, 255);
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
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(getMessage());
        if((this.input && this.config.isAutoInput()) || (!this.input && this.config.isAutoOutput()))
            tooltips.add(ENABLED);
        else
            tooltips.add(DISABLED);
        this.parent.renderComponentTooltip(pose, tooltips, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && this.isValidClickButton(button) && this.clicked(mouseX, mouseY)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            if(Minecraft.getInstance().player == null)
                return true;
            new CChangeSideModePacket(Minecraft.getInstance().player.containerMenu.containerId, getComponentId(), (byte) (this.input ? 6 : 7), true).sendToServer();
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
        if((this.input && this.config.isAutoInput()) || (!this.input && this.config.isAutoOutput()))
            output.add(NarratedElementType.HINT, ENABLED);
        else
            output.add(NarratedElementType.HINT, DISABLED);
    }
}
