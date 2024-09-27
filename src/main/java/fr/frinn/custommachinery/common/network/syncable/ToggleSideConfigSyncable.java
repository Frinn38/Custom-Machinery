package fr.frinn.custommachinery.common.network.syncable;

import fr.frinn.custommachinery.common.network.data.ToggleSideConfigData;
import fr.frinn.custommachinery.impl.component.config.ToggleSideConfig;
import fr.frinn.custommachinery.impl.network.AbstractSyncable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ToggleSideConfigSyncable extends AbstractSyncable<ToggleSideConfigData, ToggleSideConfig> {

    @Override
    public ToggleSideConfigData getData(short id) {
        return new ToggleSideConfigData(id, get());
    }

    @Override
    public boolean needSync() {
        ToggleSideConfig value = get();
        if(this.lastKnownValue == null) {
            this.lastKnownValue = value.copy();
            return true;
        }
        if(!this.lastKnownValue.equals(value)) {
            this.lastKnownValue = value.copy();
            return true;
        }
        return false;
    }

    public static ToggleSideConfigSyncable create(Supplier<ToggleSideConfig> supplier, Consumer<ToggleSideConfig> consumer) {
        return new ToggleSideConfigSyncable() {
            @Override
            public ToggleSideConfig get() {
                return supplier.get();
            }

            @Override
            public void set(ToggleSideConfig value) {
                consumer.accept(value);
            }
        };
    }
}
