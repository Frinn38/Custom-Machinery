package fr.frinn.custommachinery.client.screen.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.client.screen.widget.ListWidget.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.ArrowNavigation;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ListWidget<E extends Entry> extends AbstractWidget implements ContainerEventHandler {

    private final int itemHeight;
    private final List<E> entries = new ArrayList<>();
    private final Minecraft mc = Minecraft.getInstance();
    private final Font font = mc.font;

    @Nullable
    private E selected;
    private boolean renderSelection = false;
    private double scrollAmount;
    private boolean scrolling;
    private boolean dragging;

    public ListWidget(int x, int y, int width, int height, int itemHeight, Component message) {
        super(x, y, width, height, message);
        this.itemHeight = itemHeight;
    }

    /** LIST STUFF **/

    public List<E> getEntries() {
        return ImmutableList.copyOf(this.entries);
    }

    public void addEntry(E entry) {
        this.entries.add(entry);
    }

    public void clear() {
        this.entries.clear();
        this.selected = null;
    }

    @Nullable
    public E getSelected() {
        return this.selected;
    }

    public void setSelected(@Nullable E selected) {
        this.selected = selected;
    }

    @Nullable
    public E getEntryAtPosition(double mouseX, double mouseY) {
        int index = Mth.clamp(Mth.floor(mouseY - this.getY() + this.getScrollAmount() - 4), 0, this.getMaxPosition()) / this.itemHeight;
        if (mouseX >= this.getX() && mouseX <= this.getX() + this.width && index >= 0 && index < this.entries.size())
            return this.entries.get(index);
        return null;
    }

    @NotNull
    @Override
    public List<E> children() {
        return this.getEntries();
    }

    /** SCROLLING **/

    public int getMaxPosition() {
        return this.entries.size() * this.itemHeight;
    }

    private void scroll(int scroll) {
        this.setScrollAmount(this.getScrollAmount() + (double)scroll);
    }

    public double getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double scroll) {
        this.scrollAmount = Mth.clamp(scroll, 0.0, (double)this.getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - this.height + 4);
    }

    public void updateScrollingState(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 && mouseX >= (double)this.getScrollbarPosition() && mouseX < (double)(this.getScrollbarPosition() + 6);
    }

    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    public void ensureVisible(E entry) {
        int entryTop = this.getY() + 4 - (int)this.getScrollAmount() + this.entries.indexOf(entry) * this.itemHeight;

        int j = entryTop - this.getY() - 4 - this.itemHeight;
        if (j < 0)
            this.scroll(j);

        int k = this.getY() + this.height - entryTop - this.itemHeight * 2;
        if(k < 0)
            this.scroll(-k);
    }

    /**
     * RENDERING
     **/

    protected void setRenderSelection() {
        this.renderSelection = true;
    }

    protected void renderList(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (int index = 0; index < this.entries.size(); index++) {
            int entryTop = this.getY() + 4 - (int)this.getScrollAmount() + index * this.itemHeight;
            int entryBottom = entryTop + this.itemHeight;
            if (entryBottom < this.getY() || entryTop > this.getY() + this.height)
                continue;
            this.renderItem(graphics, mouseX, mouseY, partialTick, index, this.getX(), entryTop, this.width, this.itemHeight - 4);
        }
    }

    protected void renderItem(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
        E entry = this.entries.get(index);
        entry.renderBackground(graphics, index, left, top, width, height, mouseX, mouseY, partialTick);
        if(this.renderSelection && this.selected == entry)
            this.renderSelection(graphics, top, width - 8, height, FastColor.ARGB32.color(255, 0, 0, 0), FastColor.ARGB32.color(255, 198, 198, 198));
        entry.render(graphics, index, left, top, width, height, mouseX, mouseY, partialTick);
        for(Object children : entry.children())
            if(children instanceof Renderable renderable)
                renderable.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int barLeft = this.getScrollbarPosition();
        int barRight = barLeft + 6;

        graphics.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);
        this.renderList(graphics, mouseX, mouseY, partialTick);
        graphics.disableScissor();

        //Scrollbar
        if(this.getMaxScroll() > 0) {
            int n = (int)(this.height * this.height / (float)this.getMaxPosition());
            n = Mth.clamp(n, 32, this.height - 8);
            int o = (int)this.getScrollAmount() * (this.height - n) / this.getMaxScroll() + this.getY();
            if (o < this.getY()) {
                o = this.getY();
            }
            graphics.fill(barLeft, this.getY(), barRight, this.getY() + this.height, -16777216);
            graphics.fill(barLeft, o, barRight, o + n, -8355712);
            graphics.fill(barLeft, o, barRight - 1, o + n - 1, -4144960);
        }

        RenderSystem.disableBlend();
    }

    protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
        guiGraphics.fill(this.getX(), top - 2, this.getX() + width, top + height + 2, outerColor);
        guiGraphics.fill(this.getX() + 1, top - 1, this.getX() + width - 1, top + height + 1, innerColor);
    }

    /** GUI ELEMENT **/

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updateScrollingState(mouseX, mouseY, button);
        if(!this.isMouseOver(mouseX, mouseY))
            return false;

        E entry = this.getEntryAtPosition(mouseX, mouseY);
        if(entry != null) {
            if(entry.mouseClicked(mouseX, mouseY, button)) {
                GuiEventListener focused = this.getFocused();
                if(focused != entry && focused instanceof ContainerEventHandler container)
                    container.setFocused(null);
                this.setFocused(entry);
                this.setDragging(true);
                return true;
            }
        }
        return this.scrolling;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(this.getFocused() != null)
            this.getFocused().mouseReleased(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        if (this.getFocused() != null)
            this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);

        if(button != 0 || !this.scrolling)
            return false;

        if(mouseY < this.getY()) {
            this.setScrollAmount(0.0);
        } else if(mouseY > this.getY() + this.height) {
            this.setScrollAmount(this.getMaxScroll());
        } else {
            double d = Math.max(1, this.getMaxScroll());
            int i = this.height;
            int j = Mth.clamp((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
            double e = Math.max(1.0, d / (double)(i - j));
            this.setScrollAmount(this.getScrollAmount() + dragY * e);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.setScrollAmount(this.getScrollAmount() - delta * (double)this.itemHeight / 2.0);
        return true;
    }

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if(this.entries.isEmpty())
            return null;

        if(event instanceof ArrowNavigation arrowNavigation) {
            ComponentPath componentPath;
            E entry = this.selected;

            if (arrowNavigation.direction().getAxis() == ScreenAxis.HORIZONTAL && entry != null)
                return ComponentPath.path(this, entry.nextFocusPath(event));

            int i = -1;
            ScreenDirection screenDirection = arrowNavigation.direction();
            if (entry != null)
                i = entry.children().indexOf(entry.getFocused());

            if (i == -1) {
                switch (screenDirection) {
                    case LEFT: {
                        i = Integer.MAX_VALUE;
                        screenDirection = ScreenDirection.DOWN;
                        break;
                    }
                    case RIGHT: {
                        i = 0;
                        screenDirection = ScreenDirection.DOWN;
                        break;
                    }
                    default: {
                        i = 0;
                    }
                }
            }
            E entry2 = entry;
            do {
                if ((entry2 = this.nextEntry(screenDirection, arg -> !arg.children().isEmpty(), entry2)) != null) continue;
                return null;
            } while ((componentPath = entry2.focusPathAtIndex(arrowNavigation, i)) == null);
            return ComponentPath.path(this, componentPath);
        }
        return super.nextFocusPath(event);
    }

    @Nullable
    protected E nextEntry(ScreenDirection direction, Predicate<E> predicate, @Nullable E selected) {
        byte b0 = switch (direction) {
            case RIGHT, LEFT -> 0;
            case UP -> -1;
            case DOWN -> 1;
        };

        if (!this.children().isEmpty() && (int) b0 != 0) {
            int j;
            if (selected == null) {
                j = (int) b0 > 0 ? 0 : this.children().size() - 1;
            } else {
                j = this.children().indexOf(selected) + (int) b0;
            }

            for(int k = j; k >= 0 && k < this.entries.size(); k += b0) {
                E e = this.children().get(k);
                if (predicate.test(e)) {
                    return e;
                }
            }
        }

        return null;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        int i = this.entries.indexOf(focused);
        if(i >= 0) {
            E entry = this.entries.get(i);
            this.setSelected(entry);
            if (this.mc.getLastInputType().isKeyboard()) {
                this.ensureVisible(entry);
            }
            entry.setFocused(true);
        } else
            this.setSelected(null);
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.selected;
    }

    @Override
    public void setDragging(boolean isDragging) {
        this.dragging = isDragging;
    }

    @Override
    public boolean isDragging() {
        return this.dragging;
    }

    public static abstract class Entry implements ContainerEventHandler {

        @Nullable
        private GuiEventListener focused;
        private boolean dragging;

        protected void renderBackground(GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {

        }

        protected abstract void render(GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks);

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
            if (event instanceof ArrowNavigation arrowNavigation) {
                int i = arrowNavigation.direction() == ScreenDirection.RIGHT ? 1 : 0;
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
