package fr.frinn.custommachinery.impl.integration.jei;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.RegisterWidgetToJeiIngredientGetterEvent;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.runtime.IClickableIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class WidgetToJeiIngredientRegistry {

    private static Map<GuiElementType<?>, IngredientGetter<?>> registry;
    private static boolean init = false;

    public static void init() {
        if(init)
            return;
        RegisterWidgetToJeiIngredientGetterEvent event = new RegisterWidgetToJeiIngredientGetterEvent();
        RegisterWidgetToJeiIngredientGetterEvent.EVENT.invoker().registerIngredientGetter(event);
        registry = event.getIngredientGetters();
        init = true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static IClickableIngredient<?> getIngredient(AbstractGuiElementWidget<?> widget, double mouseX, double mouseY, IJeiHelpers helpers) {
        IngredientGetter getter = registry.get(widget.getElement().getType());
        if(getter == null)
            return null;
        return getter.getIngredient(widget, mouseX, mouseY, helpers);
    }

    public interface IngredientGetter<E extends IGuiElement> {
        @Nullable
        <T> IClickableIngredient<T> getIngredient(AbstractGuiElementWidget<E> widget, double mouseX, double mouseY, IJeiHelpers helpers);
    }
}
