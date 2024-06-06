package fr.frinn.custommachinery.client.screen.creation.appearance;

import com.google.common.collect.ImmutableMap;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Subscribe to this Event to register a builder for an appearance property.
 * This Event is fired only on the client side.
 */
public class RegisterAppearancePropertyBuilderEvent {

    public static final Event<Register> EVENT = EventFactory.createLoop();

    public Map<MachineAppearanceProperty<?>, IAppearancePropertyBuilder<?>> builders = new HashMap<>();

    public <T> void register(MachineAppearanceProperty<T> property, IAppearancePropertyBuilder<T> builder) {
        if(this.builders.containsKey(property))
            throw new IllegalArgumentException("Appearance property builder already registered for appearance property: " + property.getId());
        this.builders.put(property, builder);
    }

    public Map<MachineAppearanceProperty<?>, IAppearancePropertyBuilder<?>> getBuilders() {
        return ImmutableMap.copyOf(this.builders);
    }

    public interface Register {

        void registerAppearancePropertyBuilders(final RegisterAppearancePropertyBuilderEvent event);
    }
}