package fr.frinn.custommachinery.api.integration.jei;

import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

public class RegisterGuiElementJEIRendererEvent extends Event implements IModBusEvent {

    private final Map<GuiElementType<?>, IJEIElementRenderer<?>> renderers = new HashMap<>();

    public <E extends IGuiElement> void register(GuiElementType<E> type, IJEIElementRenderer<E> renderer) {
        if(this.renderers.containsKey(type))
            throw new IllegalArgumentException("Jei renderer already registered for Gui Element: " + type.getId());
        this.renderers.put(type, renderer);
    }

    public Map<GuiElementType<?>, IJEIElementRenderer<?>> getRenderers() {
        return ImmutableMap.copyOf(this.renderers);
    }
}
