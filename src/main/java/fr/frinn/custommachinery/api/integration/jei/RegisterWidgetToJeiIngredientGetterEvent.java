package fr.frinn.custommachinery.api.integration.jei;

import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.impl.integration.jei.WidgetToJeiIngredientRegistry.IngredientGetter;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

public class RegisterWidgetToJeiIngredientGetterEvent extends Event implements IModBusEvent {

    private final Map<GuiElementType<?>, IngredientGetter<?>> registry = new HashMap<>();

    public <E extends IGuiElement> void register(GuiElementType<E> type, IngredientGetter<E> getter) {
        if(registry.containsKey(type))
            throw new IllegalArgumentException("An ingredient getter for GuiElementType: " + type + " is already registered !");
        registry.put(type, getter);
    }

    public Map<GuiElementType<?>, IngredientGetter<?>> getIngredientGetters() {
        return ImmutableMap.copyOf(this.registry);
    }
}
