package fr.frinn.custommachinery.client.screen.widget;

import fr.frinn.custommachinery.client.screen.widget.GridListWidget.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GridListWidget<E extends Entry> extends AbstractWidget {

    private final List<E> list = new ArrayList<>();

    private int maxColumns;
    private double scrollAmount;
    private boolean scrolling = false;
    @Nullable
    private E selected;
    private boolean renderSelection = false;

    public GridListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.maxColumns = width / 20;
    }

    public void clear() {
        this.list.clear();
    }

    public void addEntry(E entry) {
        this.list.add(entry);
    }

    public void addEntry(int index, E entry) {
        this.list.add(index, entry);
    }

    public List<E> getAll() {
        return Collections.unmodifiableList(this.list);
    }

    public boolean remove(E entry) {
        return this.list.remove(entry);
    }

    public void setRenderSelection() {
        this.renderSelection = true;
    }

    public void setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
    }

    @Nullable
    public E getSelected() {
        return this.selected;
    }

    @Nullable
    public E getElementUnderMouse(double mouseX, double mouseY) {
        if(mouseX < this.getX() || mouseX > this.getX() + this.maxColumns * 20 || mouseY < this.getY() || mouseY > this.getY() + this.getHeight())
            return null;
        int index = (int)((mouseY - this.getY() + this.scrollAmount) / 20) * this.maxColumns + (int)((mouseX - this.getX()) / 20);
        if(index < 0 || index >= this.list.size())
            return null;
        return this.list.get(index);
    }

    private void scroll(int scroll) {
        this.setScrollAmount(this.getScrollAmount() + (double)scroll);
    }

    public double getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double scroll) {
        this.scrollAmount = Mth.clamp(scroll, 0.0, this.getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - this.getHeight() - 4);
    }

    protected int getMaxPosition() {
        return this.list.size() / this.maxColumns * 20;
    }

    public int getScrollBottom() {
        return (int)this.getScrollAmount() - this.getHeight();
    }

    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 && mouseX >= this.getScrollbarPosition() && mouseX < this.getScrollbarPosition() + 6;
    }

    protected int getScrollbarPosition() {
        return this.width / 2 + 124;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
        graphics.pose().pushPose();
        graphics.pose().translate(this.getX(), this.getY(), 0);
        for(int i = 0; i < this.list.size(); i++) {
            int x = (i % this.maxColumns) * 20;
            int y = i / this.maxColumns * 20 - (int)this.getScrollAmount();
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);

            E entry = this.list.get(i);

            if(this.renderSelection && this.selected == entry)
                graphics.fill(0, 0, 20, 20, FastColor.ARGB32.color(255, 255, 0, 0));

            graphics.pose().translate(0, 0, 100);

            entry.render(graphics, mouseX, mouseY, partialTick);

            graphics.pose().popPose();
        }
        graphics.pose().popPose();

        if(this.getMaxScroll() > 0) {
            int i = this.getX() + this.getWidth() - 10;
            int j = i + 6;
            int n = (this.getHeight() * this.getHeight()) / this.getMaxPosition();
            n = Mth.clamp(n, 32, this.getHeight() - 8);
            int o = (int)this.getScrollAmount() * (this.getHeight() - n) / this.getMaxScroll() + this.getY();
            if (o < this.getY()) {
                o = this.getY();
            }
            graphics.fill(i, this.getY(), j, this.getY() + this.getHeight(), -16777216);
            graphics.fill(i, o, j, o + n, -8355712);
            graphics.fill(i, o, j - 1, o + n - 1, -4144960);
        }

        graphics.disableScissor();

        E hovered = this.getElementUnderMouse(mouseX, mouseY);
        if(hovered != null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.addAll(hovered.getTooltips());
            graphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updateScrollingState(mouseX, mouseY, button);
        E selected = this.getElementUnderMouse(mouseX, mouseY);
        if(selected != null) {
            this.selected = selected;
            return selected.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
            return true;
        if (button != 0 || !this.scrolling)
            return false;
        if (mouseY < this.getY()) {
            this.setScrollAmount(0.0);
        } else if (mouseY > this.getY() + this.getHeight()) {
            this.setScrollAmount(this.getMaxScroll());
        } else {
            double d = Math.max(1, this.getMaxScroll());
            int i = this.getHeight();
            int j = Mth.clamp((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
            double e = Math.max(1.0, d / (double)(i - j));
            this.setScrollAmount(this.getScrollAmount() + dragY * e);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.setScrollAmount(this.getScrollAmount() - scrollY * 10);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public static abstract class Entry implements ContainerEventHandler {

        @Nullable
        private GuiEventListener focused;
        private boolean dragging;

        public abstract void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

        public abstract List<Component> getTooltips();

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return this.getFocused() != null && this.getFocused().mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        @Override
        public boolean isDragging() {
            return this.dragging;
        }

        @Override
        public void setDragging(boolean isDragging) {
            this.dragging = isDragging;
        }

        @Nullable
        @Override
        public GuiEventListener getFocused() {
            return this.focused;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            if(this.focused == focused)
                return;

            if (this.focused != null)
                this.focused.setFocused(false);

            if (focused != null)
                focused.setFocused(true);

            this.focused = focused;
        }

        @Nullable
        public ComponentPath focusPathAtIndex(FocusNavigationEvent event, int index) {
            if (this.children().isEmpty()) {
                return null;
            }
            ComponentPath componentPath = this.children().get(Math.min(index, this.children().size() - 1)).nextFocusPath(event);
            return ComponentPath.path(this, componentPath);
        }

        @Override
        @Nullable
        public ComponentPath nextFocusPath(FocusNavigationEvent event) {
            if (event instanceof FocusNavigationEvent.ArrowNavigation(ScreenDirection direction)) {
                int i = direction == ScreenDirection.RIGHT ? 1 : 0;
                if (i == 0)
                    return null;

                for (int k = Mth.clamp(i + this.children().indexOf(this.getFocused()), 0, this.children().size() - 1); k >= 0 && k < this.children().size(); k += i) {
                    GuiEventListener guiEventListener = this.children().get(k);
                    ComponentPath componentPath = guiEventListener.nextFocusPath(event);
                    if (componentPath == null) continue;
                    return ComponentPath.path(this, componentPath);
                }
            }
            return ContainerEventHandler.super.nextFocusPath(event);
        }
    }
}
