package fr.frinn.custommachinery.client.screen.creation.gui;

import com.google.common.collect.ImmutableMap;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;

import java.util.HashMap;
import java.util.Map;

public class RegisterGuiElementBuilderEvent {

    public static final Event<RegisterGuiElementBuilderEvent.Register> EVENT = EventFactory.createLoop();

    public Map<GuiElementType<?>, IGuiElementBuilder<?>> builders = new HashMap<>();

    public <T extends IGuiElement> void register(GuiElementType<T> type, IGuiElementBuilder<T> builder) {
        if(this.builders.containsKey(type))
            throw new IllegalArgumentException("Gui element builder already registered for component type: " + type.getId());
        this.builders.put(type, builder);
    }

    public Map<GuiElementType<?>, IGuiElementBuilder<?>> getBuilders() {
        return ImmutableMap.copyOf(this.builders);
    }

    public interface Register {

        void registerGuiElementBuilders(final RegisterGuiElementBuilderEvent event);
    }
}
