package fr.frinn.custommachinery.api.client;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.impl.client.AbstractGuiElementRenderable;

/**
 * A supplier to get a new {@link AbstractGuiElementRenderable} for a specific {@link IGuiElement}.
 * @param <T> The {@link IGuiElement} which hold all the data used by the widget to display on the machine screen.
 */
@FunctionalInterface
public interface IGuiElementRenderableSupplier<T extends IGuiElement> {

    /**
     * @param element The element which hold all the data used by the widget.
     * @param screen The machine screen which displays the widgets.
     * @return A new {@link net.minecraft.client.gui.components.Renderable} to display on the machine screen.
     */
    AbstractGuiElementRenderable<T> get(T element, IMachineScreen screen);
}
