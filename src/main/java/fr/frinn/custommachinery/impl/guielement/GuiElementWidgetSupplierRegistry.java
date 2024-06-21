package fr.frinn.custommachinery.impl.guielement;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.guielement.IGuiElementWidgetSupplier;
import fr.frinn.custommachinery.api.guielement.RegisterGuiElementWidgetSupplierEvent;
import net.neoforged.fml.ModLoader;

import java.util.Map;

public class GuiElementWidgetSupplierRegistry {

    private static Map<GuiElementType<?>, IGuiElementWidgetSupplier<?>> widgetSuppliers;

    public static void init() {
        RegisterGuiElementWidgetSupplierEvent event = new RegisterGuiElementWidgetSupplierEvent();
        ModLoader.postEventWrapContainerInModOrder(event);
        widgetSuppliers = event.getWidgetSuppliers();
    }

    public static <E extends IGuiElement> boolean hasWidgetSupplier(GuiElementType<E> type) {
        return widgetSuppliers.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public static <E extends IGuiElement> IGuiElementWidgetSupplier<E> getWidgetSupplier(GuiElementType<E> type) {
        return (IGuiElementWidgetSupplier<E>) widgetSuppliers.get(type);
    }
}
