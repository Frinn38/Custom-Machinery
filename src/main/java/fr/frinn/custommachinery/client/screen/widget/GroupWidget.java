package fr.frinn.custommachinery.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.ArrowNavigation;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GroupWidget extends AbstractWidget implements ContainerEventHandler {

    public final List<AbstractWidget> children = new ArrayList<>();
    public final Minecraft mc = Minecraft.getInstance();
    public final Font font = mc.font;

    @Nullable
    private GuiEventListener focused;
    private boolean dragging;

    public GroupWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public <T extends AbstractWidget> T addWidget(T widget) {
        this.children.add(widget);
        return widget;
    }

    @Override
    public List<AbstractWidget> children() {
        return this.children;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.children.forEach(children -> children.render(graphics, mouseX, mouseY, partialTick));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.children.forEach(children -> children.updateNarration(narrationElementOutput));
    }

    @Override
    public void setX(int x) {
        int delta = x - this.getX();
        super.setX(x);
        this.children.forEach(children -> children.setX(children.getX() + delta));
    }

    @Override
    public void setY(int y) {
        int delta = y - this.getY();
        super.setY(y);
        this.children.forEach(children -> children.setY(children.getY() + delta));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(AbstractWidget children : this.children) {
            if(children.mouseClicked(mouseX, mouseY, button)) {
                if(this.focused != null)
                    this.focused.setFocused(false);
                this.focused = children;
                children.setFocused(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for(AbstractWidget children : this.children) {
            if(children.mouseReleased(mouseX, mouseY, button))
                return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for(AbstractWidget children : this.children) {
            if(children.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
                return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(this.focused != null)
            return this.focused.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.children.forEach(children -> children.mouseMoved(mouseX, mouseY));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for(AbstractWidget children : this.children) {
            if(children.keyPressed(keyCode, scanCode, modifiers))
                return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for(AbstractWidget children : this.children) {
            if(children.keyReleased(keyCode, scanCode, modifiers))
                return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for(AbstractWidget children : this.children) {
            if(children.charTyped(codePoint, modifiers))
                return true;
        }
        return false;
    }

    @Override
    public boolean isDragging() {
        return this.dragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if(this.focused != null)
            this.focused.setFocused(false);
        if(focused != null)
            focused.setFocused(true);
        this.focused = focused;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if(!focused)
            this.setFocused(null);

    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if(event instanceof ArrowNavigation arrowNavigation) {
            GuiEventListener focused = this.getFocused();
            return switch(arrowNavigation.direction()) {
                case LEFT, UP -> {
                    if(focused == null)
                        yield ComponentPath.path(this, this.children.getLast().nextFocusPath(event));
                    else {
                        if(this.children.indexOf(focused) == 0)
                            yield null;
                        else
                            yield ComponentPath.path(this, this.children.get(this.children.indexOf(focused) - 1).nextFocusPath(event));
                    }
                }
                case RIGHT, DOWN -> {
                    if(focused == null)
                        yield ComponentPath.path(this, this.children.getFirst().nextFocusPath(event));
                    else {
                        if(this.children.indexOf(focused) == this.children.size() - 1)
                            yield null;
                        else
                            yield ComponentPath.path(this, this.children.get(this.children.indexOf(focused) + 1).nextFocusPath(event));
                    }
                }
            };
        }
        return super.nextFocusPath(event);
    }
}
