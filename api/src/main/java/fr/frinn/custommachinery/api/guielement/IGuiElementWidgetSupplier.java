package fr.frinn.custommachinery.api.guielement;

import net.minecraft.client.gui.components.AbstractWidget;

public interface IGuiElementWidgetSupplier<T extends IGuiElement> {

    AbstractWidget get(T element, IMachineScreen screen);
}
