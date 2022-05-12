package fr.frinn.custommachinery.apiimpl.guielement;

import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.RegisterGuiElementRendererEvent;
import net.minecraftforge.fml.ModLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GuiElementRendererRegistry {

    public static final DummyGuiElementRenderer DUMMY_RENDERER = new DummyGuiElementRenderer();
    private static final Logger LOGGER = LogManager.getLogger("Gui Element Renderer Registry");
    private static Map<GuiElementType<?>, IGuiElementRenderer<?>> renderers;

    /**
     * Note: don't throw here, the exception will just be ignored by forge.
     * Instead, just log an error, and register a dummy renderer to avoid NPE when trying to render an element with no renderer.
     */
    public static void init() {
        RegisterGuiElementRendererEvent event = new RegisterGuiElementRendererEvent();
        ModLoader.get().postEvent(event);
        Map<GuiElementType<?>, IGuiElementRenderer<?>> toAdd = new HashMap<>();
        ICustomMachineryAPI.INSTANCE.guiElementRegistry().getValues().forEach(type -> {
            if(!event.getRenderers().containsKey(type)) {
                LOGGER.error("No renderer registered for Gui Element: {}", type.getRegistryName());
                toAdd.put(type, DUMMY_RENDERER);
            } else toAdd.put(type, event.getRenderers().get(type));
        });
        renderers = ImmutableMap.copyOf(toAdd);
    }

    @SuppressWarnings("unchecked")
    public static <E extends IGuiElement> IGuiElementRenderer<E> getRenderer(GuiElementType<E> type) {
        return (IGuiElementRenderer<E>) renderers.getOrDefault(type, DUMMY_RENDERER);
    }
}
