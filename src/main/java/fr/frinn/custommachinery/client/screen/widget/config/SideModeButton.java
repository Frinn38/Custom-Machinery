package fr.frinn.custommachinery.client.screen.widget.config;

import fr.frinn.custommachinery.client.screen.ScreenUtils;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig.ConfigButtonData;
import fr.frinn.custommachinery.impl.component.config.SideConfig.SideMode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.function.Supplier;

public class SideModeButton extends ImageButton {

    private final Supplier<SideMode> modeGetter;
    private final RelativeSide side;
    private final OnPress leftClick;
    private final OnPress rightClick;

    public SideModeButton(int x, int y, Supplier<SideMode> modeGetter, RelativeSide side, OnPress leftClick, OnPress rightClick, ConfigButtonData data) {
        super(x + data.x(), y + data.y(), data.width(), data.height(), ScreenUtils.dataToSprite(data.sprites()), button -> {}, side.getTranslationName());
        this.modeGetter = modeGetter;
        this.side = side;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int color = this.modeGetter.get().color();
        if(ARGB.alpha(color) == 0)
            color = ARGB.white(1.0f);
        Identifier sprite = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.width, this.height, color);
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
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if(event.button() == 0)
            this.leftClick.onPress(this);
        else
            this.rightClick.onPress(this);
    }
}
