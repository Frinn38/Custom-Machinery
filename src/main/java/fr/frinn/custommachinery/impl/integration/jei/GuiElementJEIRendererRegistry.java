package fr.frinn.custommachinery.impl.integration.jei;

import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.api.integration.jei.RegisterGuiElementJEIRendererEvent;
import net.neoforged.fml.ModLoader;

import java.util.Map;

public class GuiElementJEIRendererRegistry {

    private static Map<GuiElementType<?>, IJEIElementRenderer<?>> renderers;

    public static void init() {
        RegisterGuiElementJEIRendererEvent event = new RegisterGuiElementJEIRendererEvent();
        ModLoader.postEventWrapContainerInModOrder(event);
        renderers = event.getRenderers();
    }

    public static <E extends IGuiElement> boolean hasJEIRenderer(GuiElementType<E> type) {
        return renderers.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public static <E extends IGuiElement> IJEIElementRenderer<IGuiElement> getJEIRenderer(GuiElementType<E> type) {
        return (IJEIElementRenderer<IGuiElement>) renderers.get(type);
    }
}
