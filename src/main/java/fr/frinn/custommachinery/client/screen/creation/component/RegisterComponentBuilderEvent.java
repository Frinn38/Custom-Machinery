package fr.frinn.custommachinery.client.screen.creation.component;

import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Subscribe to this Event to register a builder for a machine component.
 * This Event is fired only on the client side.
 */
public class RegisterComponentBuilderEvent extends Event implements IModBusEvent {

    public Map<MachineComponentType<?>, IMachineComponentBuilder<?, ?>> builders = new HashMap<>();

    public <C extends IMachineComponent, T extends IMachineComponentTemplate<C>> void register(MachineComponentType<C> type, IMachineComponentBuilder<C, T> builder) {
        if(this.builders.containsKey(type))
            throw new IllegalArgumentException("Machine component builder already registered for component type: " + type.getId());
        this.builders.put(type, builder);
    }

    public Map<MachineComponentType<?>, IMachineComponentBuilder<?, ?>> getBuilders() {
        return ImmutableMap.copyOf(this.builders);
    }
}
