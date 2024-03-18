package fr.frinn.custommachinery.client.screen.popup;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class PopupScreen extends BaseScreen {
    private BaseScreen parent;

    protected PopupScreen(int xSize, int ySize) {
        super(Component.literal("Popup"), xSize, ySize);
    }

    public void setParent(BaseScreen parent) {
        this.parent = parent;
    }

    public void close() {
        this.parent.closePopup();
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        blankBackground(graphics, getX(), getY(), this.xSize, this.ySize);
    }
}
