package fr.frinn.custommachinery.common.machine.builder;

import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public class MachineAppearanceBuilder {

    @Nullable
    private final MachineStatus status;
    private final Map<MachineAppearanceProperty<?>, Object> properties;

    public MachineAppearanceBuilder(@Nullable MachineStatus status) {
        this.status = status;
        Map<MachineAppearanceProperty<?>, Object> map = new HashMap<>();
        for(MachineAppearanceProperty<?> property : Registration.APPEARANCE_PROPERTY_REGISTRY)
            map.put(property, property.getDefaultValue());
        this.properties = map;
    }

    public MachineAppearanceBuilder(Map<MachineAppearanceProperty<?>, Object> properties, @Nullable MachineStatus status) {
        this.status = status;
        Map<MachineAppearanceProperty<?>, Object> map = new HashMap<>();
        for(MachineAppearanceProperty<?> property : Registration.APPEARANCE_PROPERTY_REGISTRY)
            if(!properties.containsKey(property) || properties.get(property) == null)
                map.put(property, property.getDefaultValue());
            else
                map.put(property, properties.get(property));
        this.properties = map;
    }

    @Nullable
    public MachineStatus getStatus() {
        return this.status;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(MachineAppearanceProperty<T> property) {
        if(!this.properties.containsKey(property))
            return property.getDefaultValue();
        return (T)this.properties.get(property);
    }

    public <T> void setProperty(MachineAppearanceProperty<T> property, T value) {
        this.properties.put(property, value);
    }

    public MachineAppearance build() {
        return new MachineAppearance(ImmutableMap.copyOf(this.properties));
    }
}
