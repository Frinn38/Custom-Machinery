package fr.frinn.custommachinery.api.client;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IGuiElementRenderable<T extends IGuiElement> extends Renderable {

    T getElement();

    IMachineScreen getScreen();

    ScreenRectangle getRectangle();

    void setPosition(int x, int y);

    default int getX() {
        return this.getRectangle().left();
    }

    default int getY() {
        return this.getRectangle().top();
    }

    default int getWidth() {
        return this.getRectangle().width();
    }

    default int getHeight() {
        return this.getRectangle().height();
    }

    default boolean isMouseOver(double mouseX, double mouseY) {
        return this.getRectangle().containsPoint((int)mouseX, (int)mouseY);
    }

    default List<Component> getTooltips() {
        return this.getElement().getTooltips();
    }
}
