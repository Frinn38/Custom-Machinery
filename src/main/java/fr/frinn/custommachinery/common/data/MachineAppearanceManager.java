package fr.frinn.custommachinery.common.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.Registration;

import java.util.Map;

public class MachineAppearanceManager {

    public static final Codec<MachineAppearanceManager> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    MachineAppearance.CODEC.forGetter(manager -> manager.defaultProperties),
                    MachineAppearance.CODEC.fieldOf("idle").orElse(Maps.newHashMap()).forGetter(manager -> manager.idle.getProperties()),
                    MachineAppearance.CODEC.fieldOf("running").orElse(Maps.newHashMap()).forGetter(manager -> manager.running.getProperties()),
                    MachineAppearance.CODEC.fieldOf("errored").orElse(Maps.newHashMap()).forGetter(manager -> manager.errored.getProperties()),
                    MachineAppearance.CODEC.fieldOf("paused").orElse(Maps.newHashMap()).forGetter(manager -> manager.paused.getProperties())
            ).apply(builder, (defaults, idle, running, errored, paused) -> {
                MachineAppearance idleAppearance = buildAppearance(defaults, idle);
                MachineAppearance runningAppearance = buildAppearance(defaults, running);
                MachineAppearance erroredAppearance = buildAppearance(defaults, errored);
                MachineAppearance pausedAppearance = buildAppearance(defaults, paused);
                return new MachineAppearanceManager(defaults, idleAppearance, runningAppearance, erroredAppearance, pausedAppearance);
            })
    );

    private final Map<MachineAppearanceProperty<?>, Object> defaultProperties;
    private final MachineAppearance idle;
    private final MachineAppearance running;
    private final MachineAppearance errored;
    private final MachineAppearance paused;

    public MachineAppearanceManager(Map<MachineAppearanceProperty<?>, Object> defaultProperties, MachineAppearance idle, MachineAppearance running, MachineAppearance errored, MachineAppearance paused) {
        this.defaultProperties = defaultProperties;
        this.idle = idle;
        this.running = running;
        this.errored = errored;
        this.paused = paused;
    }

    public MachineAppearance getAppearance(MachineStatus status) {
        switch (status) {
            case IDLE:
                return this.idle;
            case RUNNING:
                return this.running;
            case ERRORED:
                return this.errored;
            case PAUSED:
                return this.paused;
        }
        throw new IllegalArgumentException("Invalid machine status: " + status);
    }

    private static MachineAppearance buildAppearance(Map<MachineAppearanceProperty<?>, Object> defaults, Map<MachineAppearanceProperty<?>, Object> specifics) {
        ImmutableMap.Builder<MachineAppearanceProperty<?>, Object> properties = ImmutableMap.builder();
        for(MachineAppearanceProperty<?> property : Registration.APPEARANCE_PROPERTY_REGISTRY.get()) {
            Object value = specifics.get(property);
            if(value == null)
                properties.put(property, defaults.get(property));
            else
                properties.put(property, value);
        }
        return new MachineAppearance(properties.build());
    }
}
