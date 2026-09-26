package fr.frinn.custommachinery.client.screen.popup;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class PopupScreen extends BaseScreen {

    public static final Component CONFIRM = Component.translatable("custommachinery.gui.popup.confirm").withStyle(ChatFormatting.GREEN);
    public static final Component CANCEL = Component.translatable("custommachinery.gui.popup.cancel").withStyle(ChatFormatting.RED);

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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if((int) Math.abs(this.dragX) >= 1 || (int) Math.abs(this.dragY) >= 1) {
            int changedX = (int) this.dragX;
            int changedY = (int) this.dragY;
            this.move(changedX, changedY);
            this.dragX -= changedX;
            this.dragY -= changedY;
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        blankBackground(graphics, this.x, this.y, this.xSize, this.ySize);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(super.mouseClicked(event, doubleClick))
            return true;
        if(isMouseOver(event.x(), event.y()) && event.y() < this.y + 20) {
            this.dragging = true;
            return true;
        } else {
            this.setDragging(false);
            return false;
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.dragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if(this.dragging) {
            this.dragX += deltaX;
            this.dragY += deltaY;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.parent.getPopupUnderMouse(mouseX, mouseY) == this;
    }
}
