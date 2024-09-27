package fr.frinn.custommachinery.client.screen.widget.config;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.network.CChangeSideModePacket;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import fr.frinn.custommachinery.impl.component.config.SideConfig.SideMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Function;
import java.util.function.Supplier;

public class SideModeButton extends ImageButton {

    private static final WidgetSprites SPRITES = new WidgetSprites(CustomMachinery.rl("config/side_mode_button"), CustomMachinery.rl("config/side_mode_button_hovered"));

    private final Supplier<SideMode> modeGetter;
    private final RelativeSide side;
    private final OnPress leftClick;
    private final OnPress rightClick;

    public SideModeButton(int x, int y, Supplier<SideMode> modeGetter, RelativeSide side, OnPress leftClick, OnPress rightClick) {
        super(x, y, 14, 14, SPRITES, button -> {}, side.getTranslationName());
        this.modeGetter = modeGetter;
        this.side = side;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int color = this.modeGetter.get().color();
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
        tooltip.append(this.modeGetter.get().title());
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!this.clicked(mouseX, mouseY) || Minecraft.getInstance().player == null)
            return false;
        playDownSound(Minecraft.getInstance().getSoundManager());
        if (button == 0)
            this.leftClick.onPress(this);
        else
            this.rightClick.onPress(this);
        return true;
    }
}
