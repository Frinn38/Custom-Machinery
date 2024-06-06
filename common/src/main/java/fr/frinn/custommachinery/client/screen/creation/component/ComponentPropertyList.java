package fr.frinn.custommachinery.client.screen.creation.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class ComponentPropertyList extends ContainerObjectSelectionList<ComponentPropertyList.ComponentPropertyEntry> {


    public ComponentPropertyList(Minecraft minecraft, int x, int y, int width, int height, int itemHeight) {
        super(minecraft, width, height, y, y + height, itemHeight);
        this.setLeftPos(x);
        this.setRenderHeader(false, 0);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    public <T extends AbstractWidget> T add(Component title, T widget) {
        this.addEntry(new ComponentPropertyEntry(title, widget));
        return widget;
    }

    public void move(int movedX, int movedY) {
        this.setLeftPos(this.x0 + movedX);
        this.y0 = this.y0 + movedY;
        this.y1 = this.y0 + this.height;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getRowRight();
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected void renderDecorations(GuiGraphics graphics, int mouseX, int mouseY) {
        this.children().stream().map(entry -> entry.widget)
                .filter(widget -> widget.getTooltip() != null && widget.isMouseOver(mouseX, mouseY))
                .forEach(widget -> graphics.renderTooltip(Minecraft.getInstance().font, widget.getTooltip().toCharSequence(Minecraft.getInstance()), mouseX, mouseY));
    }

    public static class ComponentPropertyEntry extends Entry<ComponentPropertyEntry> {

        private final Component title;
        private final AbstractWidget widget;

        public ComponentPropertyEntry(Component title, AbstractWidget widget) {
            this.title = title;
            this.widget = widget;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.singletonList(this.widget);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.drawString(Minecraft.getInstance().font, this.title, left, top + (height - Minecraft.getInstance().font.lineHeight) / 2, 0, false);
            this.widget.setPosition(left + width - this.widget.getWidth() - 5, top + (height - this.widget.getHeight()) / 2);
            this.widget.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(this.widget);
        }
    }
}
