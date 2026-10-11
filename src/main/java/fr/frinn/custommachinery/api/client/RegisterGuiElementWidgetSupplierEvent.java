package fr.frinn.custommachinery.api.client;

import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Subscribe to this Event to register a widget supplier or a renderable supplier for your gui element.
 * This Event is fired only on the client side.
 */
public class RegisterGuiElementWidgetSupplierEvent extends Event implements IModBusEvent {

    private final Map<GuiElementType<?>, IGuiElementWidgetSupplier<?>> widgetSuppliers = new HashMap<>();
    private final Map<GuiElementType<?>, IGuiElementRenderableSupplier<?>> renderableSuppliers = new HashMap<>();

    /**
     * Register an {@link IGuiElementWidgetSupplier} for a specific {@link GuiElementType}.
     * A {@link GuiElementType} can have only one {@link IGuiElementWidgetSupplier} OR one {@link IGuiElementRenderableSupplier} registered.
     * Widgets are used for elements that needs mouse/keyboard interaction, for widgets that doesn't need it use {@link RegisterGuiElementWidgetSupplierEvent#registerRenderable(GuiElementType, IGuiElementRenderableSupplier)} instead.
     * @param type The type of gui element which will use the widget.
     * @param widgetSupplier The {@link IGuiElementWidgetSupplier} which will be used to create the widget which will displays the element in the machine screen.
     * @param <E> The {@link IGuiElement} which hold the data used by the widget to be displayed (position, size...)
     */
    public <E extends IGuiElement> void registerWidget(GuiElementType<E> type, IGuiElementWidgetSupplier<E> widgetSupplier) {
        if(this.widgetSuppliers.containsKey(type) || this.renderableSuppliers.containsKey(type))
            throw new IllegalArgumentException("Widget supplier already registered for Gui Element: " + type.getId());
        this.widgetSuppliers.put(type, widgetSupplier);
    }

    /**
     * Register an {@link IGuiElementRenderableSupplier} for a specific {@link GuiElementType}.
     * A {@link GuiElementType} can have only one {@link IGuiElementRenderableSupplier} OR one {@link IGuiElementWidgetSupplier} registered.
     * Renderable are used only for widget that only display something, for widgets that need mouse/keyboard interaction use {@link RegisterGuiElementWidgetSupplierEvent#registerWidget(GuiElementType, IGuiElementWidgetSupplier)} instead.
     * @param type The type of gui element which will use the widget.
     * @param renderableSupplier The {@link IGuiElementRenderableSupplier} which will be used to create the renderable which will displays the element in the machine screen.
     * @param <E> The {@link IGuiElement} which hold the data used by the widget to be displayed (position, size...)
     */
    public <E extends IGuiElement> void registerRenderable(GuiElementType<E> type, IGuiElementRenderableSupplier<E> renderableSupplier) {
        if(this.renderableSuppliers.containsKey(type) || this.widgetSuppliers.containsKey(type))
            throw new IllegalArgumentException("Renderable supplier already registered for Gui Element: " + type.getId());
        this.renderableSuppliers.put(type, renderableSupplier);
    }

    /**
     * @return A map of all registered {@link IGuiElementWidgetSupplier}.
     */
    public Map<GuiElementType<?>, IGuiElementWidgetSupplier<?>> getWidgetSuppliers() {
        return ImmutableMap.copyOf(this.widgetSuppliers);
    }

    /**
     * @return A map of all registered {@link IGuiElementRenderableSupplier}.
     */
    public Map<GuiElementType<?>, IGuiElementRenderableSupplier<?>> getRenderableSuppliers() {
        return ImmutableMap.copyOf(this.renderableSuppliers);
    }
}
