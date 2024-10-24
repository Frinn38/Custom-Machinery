package fr.frinn.custommachinery.client.screen.popup;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;

public abstract class PopupScreen extends BaseScreen {

    public final BaseScreen parent;

    private boolean dragging;
    private double dragX;
    private double dragY;

    public PopupScreen(BaseScreen parent, int xSize, int ySize) {
        super(Component.literal("Popup"), xSize, ySize);
        this.parent = parent;
    }

    public void closed() {

    }

    public void move(int movedX, int movedY) {
        this.x += movedX;
        this.y += movedY;
        this.children().forEach(c -> {
            if(c instanceof LayoutElement widget)
                widget.setPosition(widget.getX() + movedX, widget.getY() + movedY);
        });
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - this.xSize) / 2;
        this.y = (this.height - this.ySize) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if((int) Math.abs(this.dragX) >= 1 || (int) Math.abs(this.dragY) >= 1) {
            int changedX = (int) this.dragX;
            int changedY = (int) this.dragY;
            this.move(changedX, changedY);
            this.dragX -= changedX;
            this.dragY -= changedY;
        }
        boolean parentHasTooltip = this.parent.deferredTooltipRendering != null;
        super.render(graphics, mouseX, mouseY, partialTicks);
        //If parent didn't have tooltip before rendering this popup, but now have it that means the tooltips were added by this popup.
        //So we move them to this popup tooltips
        if(!parentHasTooltip && this.parent.deferredTooltipRendering != null) {
            if(this.deferredTooltipRendering == null)
                this.deferredTooltipRendering = this.parent.deferredTooltipRendering;
            this.parent.deferredTooltipRendering = null;
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        blankBackground(graphics, this.x, this.y, this.xSize, this.ySize);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(super.mouseClicked(mouseX, mouseY, button))
            return true;
        if(isMouseOver(mouseX, mouseY) && mouseY < this.y + 20) {
            this.dragging = true;
            return true;
        } else {
            this.setDragging(false);
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(this.dragging) {
            this.dragX += deltaX;
            this.dragY += deltaY;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.parent.getPopupUnderMouse(mouseX, mouseY) == this;
    }
}
