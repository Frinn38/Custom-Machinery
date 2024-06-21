package fr.frinn.custommachinery.common.machine;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.Registration;

import java.util.Map;

public class MachineAppearanceManager {

    public static final NamedCodec<MachineAppearanceManager> CODEC = NamedCodec.record(builder ->
            builder.group(
                    MachineAppearance.CODEC.forGetter(manager -> manager.defaultProperties),
                    MachineAppearance.CODEC.optionalFieldOf("idle", Maps.newHashMap()).forGetter(manager -> manager.getStatusProperties(MachineStatus.IDLE)),
                    MachineAppearance.CODEC.optionalFieldOf("running", Maps.newHashMap()).forGetter(manager -> manager.getStatusProperties(MachineStatus.RUNNING)),
                    MachineAppearance.CODEC.optionalFieldOf("errored", Maps.newHashMap()).forGetter(manager -> manager.getStatusProperties(MachineStatus.ERRORED)),
                    MachineAppearance.CODEC.optionalFieldOf("paused", Maps.newHashMap()).forGetter(manager -> manager.getStatusProperties(MachineStatus.PAUSED))
            ).apply(builder, (defaults, idle, running, errored, paused) -> {
                MachineAppearance idleAppearance = buildAppearance(defaults, idle);
                MachineAppearance runningAppearance = buildAppearance(defaults, running);
                MachineAppearance erroredAppearance = buildAppearance(defaults, errored);
                MachineAppearance pausedAppearance = buildAppearance(defaults, paused);
                return new MachineAppearanceManager(defaults, idleAppearance, runningAppearance, erroredAppearance, pausedAppearance);
            }),
            "Machine appearance"
    );

    public static final MachineAppearanceManager DEFAULT = new MachineAppearanceManager(MachineAppearance.defaultProperties(), MachineAppearance.DEFAULT, MachineAppearance.DEFAULT, MachineAppearance.DEFAULT, MachineAppearance.DEFAULT);

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
        return switch (status) {
            case IDLE -> this.idle;
            case RUNNING -> this.running;
            case ERRORED -> this.errored;
            case PAUSED -> this.paused;
        };
    }

    private static MachineAppearance buildAppearance(Map<MachineAppearanceProperty<?>, Object> defaults, Map<MachineAppearanceProperty<?>, Object> specifics) {
        ImmutableMap.Builder<MachineAppearanceProperty<?>, Object> properties = ImmutableMap.builder();
        for(MachineAppearanceProperty<?> property : Registration.APPEARANCE_PROPERTY_REGISTRY) {
            Object value = specifics.get(property);
            if(value == null || value == property.getDefaultValue())
                properties.put(property, defaults.get(property));
            else
                properties.put(property, value);
        }
        return new MachineAppearance(properties.build());
    }

    public Map<MachineAppearanceProperty<?>, Object> getDefaultProperties() {
        return this.defaultProperties;
    }

    public Map<MachineAppearanceProperty<?>, Object> getStatusProperties(MachineStatus status) {
        MachineAppearance appearance = this.getAppearance(status);
        ImmutableMap.Builder<MachineAppearanceProperty<?>, Object> map = ImmutableMap.builder();
        appearance.getProperties().forEach((property, value) -> {
            if(!value.equals(property.getDefaultValue()) && !value.equals(this.defaultProperties.get(property)))
                map.put(property, value);
        });
        return map.build();
    }
}
