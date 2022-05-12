package fr.frinn.custommachinery.api.guielement;

import com.google.common.collect.ImmutableMap;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Subscribe to this Event to register a renderer for your gui element.
 * This Event is fired on the Mod bus (FMLJavaModLoadingContext.get().getModEventBus()).
 * This Event is fired only on the client side.
 */
public class RegisterGuiElementRendererEvent extends Event implements IModBusEvent {

    private final Map<GuiElementType<?>, IGuiElementRenderer<?>> renderers = new HashMap<>();

    public <E extends IGuiElement> void register(GuiElementType<E> type, IGuiElementRenderer<E> renderer) {
        if(this.renderers.containsKey(type))
            throw new IllegalArgumentException("Renderer already registered for Gui Element: " + type.getRegistryName());
        this.renderers.put(type, renderer);
    }

    public Map<GuiElementType<?>, IGuiElementRenderer<?>> getRenderers() {
        return ImmutableMap.copyOf(this.renderers);
    }
}
