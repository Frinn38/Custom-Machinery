package fr.frinn.custommachinery.impl.client;

import fr.frinn.custommachinery.api.client.IGuiElementRenderable;
import fr.frinn.custommachinery.api.client.IMachineScreen;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.screen.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class AbstractGuiElementRenderable<T extends IGuiElement> implements IGuiElementRenderable<T> {

    private final T element;
    private final IMachineScreen screen;
    private final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();

    private ScreenRectangle rectangle;

    public AbstractGuiElementRenderable(T element, IMachineScreen screen) {
        this.element = element;
        this.screen = screen;
        this.rectangle = new ScreenRectangle(element.getX() + screen.getX(), element.getY() + screen.getY(), element.getWidth(), element.getHeight());
    }

    @Override
    public T getElement() {
        return this.element;
    }

    @Override
    public IMachineScreen getScreen() {
        return this.screen;
    }

    @Override
    public ScreenRectangle getRectangle() {
        return this.rectangle;
    }

    @Override
    public void setPosition(int x, int y) {
        this.rectangle = new ScreenRectangle(x, y, this.getWidth(), this.getHeight());
    }

    @Override
    public final void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.screen.getX(), this.screen.getY());
        this.extractRenderableRenderState(graphics, mouseX, mouseY, partialTicks);
        List<Component> tooltips = this.getTooltips();
        if(!tooltips.isEmpty()) {
            this.tooltip.set(Tooltip.create(ScreenUtils.merge(this.getTooltips())));
            this.tooltip.refreshTooltipForNextRenderPass(graphics, mouseX, mouseY, this.isMouseOver(mouseX, mouseY), false, this.rectangle);
        }
        graphics.pose().popMatrix();
    }

    public abstract void extractRenderableRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks);
}
