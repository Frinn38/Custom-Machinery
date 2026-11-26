package fr.frinn.custommachinery.api.client;

import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.impl.client.AbstractGuiElementRenderable;
import fr.frinn.custommachinery.impl.client.AbstractGuiElementWidget;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Subscribe to this Event to register a widget supplier or a renderable supplier for your gui element.
 * This Event is fired only on the client side.
 */
public class RegisterGuiElementWidgetSupplierEvent extends Event implements IModBusEvent {

    private final Map<GuiElementType<?>, IGuiElementRenderableSupplier<?>> renderableSuppliers = new HashMap<>();

    /**
     * Register an {@link IGuiElementRenderableSupplier} for a specific {@link GuiElementType}.
     * A {@link GuiElementType} can have only one {@link IGuiElementRenderableSupplier} registered.
     * The {@link IGuiElementRenderable} can be either an instance of {@link AbstractGuiElementRenderable} if no inputs (mouse/keyboards) needs to be handled
     * or an instance of {@link AbstractGuiElementWidget} otherwise.
     * @param type The type of gui element which will use the widget.
     * @param renderableSupplier The {@link IGuiElementRenderableSupplier} which will be used to create the renderable which will displays the element in the machine screen.
     * @param <E> The {@link IGuiElement} which hold the data used by the widget to be displayed (position, size...)
     */
    public <E extends IGuiElement> void registerRenderable(GuiElementType<E> type, IGuiElementRenderableSupplier<E> renderableSupplier) {
        if(this.renderableSuppliers.containsKey(type))
            throw new IllegalArgumentException("Renderable supplier already registered for Gui Element: " + type.getId());
        this.renderableSuppliers.put(type, renderableSupplier);
    }

    /**
     * @return A map of all registered {@link IGuiElementRenderableSupplier}.
     */
    public Map<GuiElementType<?>, IGuiElementRenderableSupplier<?>> getRenderableSuppliers() {
        return ImmutableMap.copyOf(this.renderableSuppliers);
    }
}
