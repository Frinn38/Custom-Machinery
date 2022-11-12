package fr.frinn.custommachinery.api.component.variant;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RegisterComponentVariantEvent {

    public static final Event<Register> EVENT = EventFactory.createLoop();

    private final Map<MachineComponentType<? extends IMachineComponent>, Map<ResourceLocation, Codec<? extends IComponentVariant>>> componentVariants = new HashMap<>();

    public <C extends IMachineComponent> void register(MachineComponentType<C> type, ResourceLocation id, Codec<? extends IComponentVariant> codec) {
        if(this.componentVariants.computeIfAbsent(type, t -> new HashMap<>()).containsKey(id))
            throw new IllegalArgumentException("Component variant " + id + " already registered for type: " + type.getId());
        this.componentVariants.get(type).put(id, codec);
    }

    public Map<MachineComponentType<? extends IMachineComponent>, Map<ResourceLocation, Codec<? extends IComponentVariant>>> getComponentVariants() {
        ImmutableMap.Builder<MachineComponentType<? extends IMachineComponent>, Map<ResourceLocation, Codec<? extends IComponentVariant>>> builder = ImmutableMap.builder();
        this.componentVariants.forEach((type, map) -> builder.put(type, ImmutableMap.copyOf(map)));
        return builder.build();
    }

    public interface Register {

        void registerComponentVariant(RegisterComponentVariantEvent event);
    }
}
