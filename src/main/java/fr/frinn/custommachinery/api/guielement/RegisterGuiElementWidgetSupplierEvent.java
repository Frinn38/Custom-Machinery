package fr.frinn.custommachinery.api.guielement;

import com.google.common.collect.ImmutableMap;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Subscribe to this Event to register a widget supplier for your gui element.
 * This Event is fired only on the client side.
 */
public class RegisterGuiElementWidgetSupplierEvent extends Event implements IModBusEvent {

    private final Map<GuiElementType<?>, IGuiElementWidgetSupplier<?>> widgetSuppliers = new HashMap<>();

    /**
     * Register an {@link IGuiElementWidgetSupplier} for a specific {@link GuiElementType}.
     * A {@link GuiElementType} can have only one {@link IGuiElementWidgetSupplier} registered.
     * @param type The type of gui element which will use the widget.
     * @param widgetSupplier The {@link IGuiElementWidgetSupplier} which will be used to create the widget which will displays the element in the machine screen.
     * @param <E> The {@link IGuiElement} which hold the data used by the widget to be displayed (position, size...)
     */
    public <E extends IGuiElement> void register(GuiElementType<E> type, IGuiElementWidgetSupplier<E> widgetSupplier) {
        if(this.widgetSuppliers.containsKey(type))
            throw new IllegalArgumentException("Widget supplier already registered for Gui Element: " + type.getId());
        this.widgetSuppliers.put(type, widgetSupplier);
    }

    /**
     * @return A map of all registered {@link IGuiElementWidgetSupplier}.
     */
    public Map<GuiElementType<?>, IGuiElementWidgetSupplier<?>> getWidgetSuppliers() {
        return ImmutableMap.copyOf(this.widgetSuppliers);
    }
}
