package fr.frinn.custommachinery.client.screen.widget.config;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig.ConfigButtonData;
import fr.frinn.custommachinery.impl.component.config.SideConfig.SideMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;

import java.util.function.Supplier;

public class SideModeButton extends ImageButton {

    private final Supplier<SideMode> modeGetter;
    private final RelativeSide side;
    private final OnPress leftClick;
    private final OnPress rightClick;

    public SideModeButton(int x, int y, Supplier<SideMode> modeGetter, RelativeSide side, OnPress leftClick, OnPress rightClick, ConfigButtonData data) {
        super(x + data.x(), y + data.y(), data.width(), data.height(), ClientHandler.dataToSprite(data.sprites()), button -> {}, side.getTranslationName());
        this.modeGetter = modeGetter;
        this.side = side;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int color = this.modeGetter.get().color();
        float r = ARGB.red(color) / 255.0F;
        float g = ARGB.green(color) / 255.0F;
        float b = ARGB.blue(color) / 255.0F;
        //RenderSystem.setShaderColor(r, g, b, 1);
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        //RenderSystem.setShaderColor(1, 1, 1, 1);
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
