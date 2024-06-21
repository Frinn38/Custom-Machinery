package fr.frinn.custommachinery.api.component.variant;

import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

public class RegisterComponentVariantEvent extends Event implements IModBusEvent {

    private final Map<MachineComponentType<? extends IMachineComponent>, Map<ResourceLocation, NamedCodec<IComponentVariant>>> componentVariants = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <C extends IMachineComponent> void register(MachineComponentType<C> type, ResourceLocation id, NamedCodec<? extends IComponentVariant> codec) {
        if(this.componentVariants.computeIfAbsent(type, t -> new HashMap<>()).containsKey(id))
            throw new IllegalArgumentException("Component variant " + id + " already registered for type: " + type.getId());
        this.componentVariants.get(type).put(id, (NamedCodec<IComponentVariant>)codec);
    }

    public Map<MachineComponentType<? extends IMachineComponent>, Map<ResourceLocation, NamedCodec<IComponentVariant>>> getComponentVariants() {
        ImmutableMap.Builder<MachineComponentType<? extends IMachineComponent>, Map<ResourceLocation, NamedCodec<IComponentVariant>>> builder = ImmutableMap.builder();
        this.componentVariants.forEach((type, map) -> builder.put(type, ImmutableMap.copyOf(map)));
        return builder.build();
    }
}
