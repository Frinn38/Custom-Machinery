package fr.frinn.custommachinery.client.screen.creation.gui;

import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

public class RegisterGuiElementBuilderEvent extends Event implements IModBusEvent {

    public Map<GuiElementType<?>, IGuiElementBuilder<?>> builders = new HashMap<>();

    public <T extends IGuiElement> void register(GuiElementType<T> type, IGuiElementBuilder<T> builder) {
        if(this.builders.containsKey(type))
            throw new IllegalArgumentException("Gui element builder already registered for component type: " + type.getId());
        this.builders.put(type, builder);
    }

    public Map<GuiElementType<?>, IGuiElementBuilder<?>> getBuilders() {
        return ImmutableMap.copyOf(this.builders);
    }
}
