package fr.frinn.custommachinery.impl.client;

import fr.frinn.custommachinery.api.client.IGuiElementRenderableSupplier;
import fr.frinn.custommachinery.api.client.RegisterGuiElementWidgetSupplierEvent;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GuiElementWidgetSupplierRegistry {

    private static Map<GuiElementType<?>, IGuiElementRenderableSupplier<?>> renderableSuppliers = new HashMap<>();

    public static void init() {
        RegisterGuiElementWidgetSupplierEvent event = new RegisterGuiElementWidgetSupplierEvent();
        ModLoader.postEventWrapContainerInModOrder(event);
        renderableSuppliers = event.getRenderableSuppliers();
    }

    public static <E extends IGuiElement> boolean hasSupplier(GuiElementType<E> type) {
        return renderableSuppliers.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <E extends IGuiElement> IGuiElementRenderableSupplier<E> getRenderableSupplier(GuiElementType<E> type) {
        return (IGuiElementRenderableSupplier<E>) renderableSuppliers.get(type);
    }
}
