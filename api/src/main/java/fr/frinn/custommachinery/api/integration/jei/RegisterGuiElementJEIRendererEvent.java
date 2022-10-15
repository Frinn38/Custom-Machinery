package fr.frinn.custommachinery.api.integration.jei;

import com.google.common.collect.ImmutableMap;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;

import java.util.HashMap;
import java.util.Map;

public class RegisterGuiElementJEIRendererEvent {

    public static final Event<Register> EVENT = EventFactory.createLoop();

    private final Map<GuiElementType<?>, IJEIElementRenderer<?>> renderers = new HashMap<>();

    public <E extends IGuiElement> void register(GuiElementType<E> type, IJEIElementRenderer<E> renderer) {
        if(this.renderers.containsKey(type))
            throw new IllegalArgumentException("Jei renderer already registered for Gui Element: " + type.getId());
        this.renderers.put(type, renderer);
    }

    public Map<GuiElementType<?>, IJEIElementRenderer<?>> getRenderers() {
        return ImmutableMap.copyOf(this.renderers);
    }

    public interface Register {
        void registerRenderers(RegisterGuiElementJEIRendererEvent event);
    }
}
