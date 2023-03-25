package fr.frinn.custommachinery.api.guielement;

import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;

public interface IGuiElementWidgetSupplier<T extends IGuiElement> {

    AbstractGuiElementWidget<T> get(T element, IMachineScreen screen);
}
