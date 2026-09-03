package fr.frinn.custommachinery.api.guielement;

import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;

/**
 * A supplier to get a new {@link AbstractGuiElementWidget} for a specific {@link IGuiElement}.
 * @param <T> The {@link IGuiElement} which hold all the data used by the widget to display on the machine screen.
 */
public interface IGuiElementWidgetSupplier<T extends IGuiElement> {

    /**
     * @param element The element which hold all the data used by the widget.
     * @param screen The machine screen which displays the widgets.
     * @return A new widget to display on the machine screen.
     */
    AbstractGuiElementWidget<T> get(T element, IMachineScreen screen);
}
