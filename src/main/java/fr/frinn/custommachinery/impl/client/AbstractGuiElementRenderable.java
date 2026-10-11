package fr.frinn.custommachinery.impl.client;

import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.ClientHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class AbstractGuiElementRenderable<T extends IGuiElement> implements Renderable {

    private final T element;
    private final IMachineScreen screen;
    private final ScreenRectangle rectangle;
    private final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();

    public AbstractGuiElementRenderable(T element, IMachineScreen screen) {
        this.element = element;
        this.screen = screen;
        this.rectangle = new ScreenRectangle(element.getX() + screen.getX(), element.getY() + screen.getY(), element.getWidth(), element.getHeight());
    }

    public T getElement() {
        return this.element;
    }

    public IMachineScreen getScreen() {
        return this.screen;
    }

    public ScreenRectangle getRectangle() {
        return this.rectangle;
    }

    public List<Component> getTooltips() {
        return this.element.getTooltips();
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return this.rectangle.containsPoint(mouseX, mouseY);
    }

    @Override
    public final void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.screen.getX(), this.screen.getY());
        this.extractRenderableRenderState(graphics, mouseX, mouseY, partialTicks);
        this.tooltip.set(Tooltip.create(ClientHandler.merge(this.getTooltips())));
        this.tooltip.refreshTooltipForNextRenderPass(graphics, mouseX, mouseY, this.isHovered(mouseX, mouseY), false, this.rectangle);
        graphics.pose().popMatrix();
    }

    public abstract void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks);
}
