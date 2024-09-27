package fr.frinn.custommachinery.common.network.syncable;

import fr.frinn.custommachinery.common.network.data.IOSideConfigData;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import fr.frinn.custommachinery.impl.component.config.ToggleSideConfig;
import fr.frinn.custommachinery.impl.network.AbstractSyncable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class IOSideConfigSyncable extends AbstractSyncable<IOSideConfigData, IOSideConfig> {

    @Override
    public IOSideConfigData getData(short id) {
        return new IOSideConfigData(id, get());
    }

    @Override
    public boolean needSync() {
        IOSideConfig value = get();
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

    public static IOSideConfigSyncable create(Supplier<IOSideConfig> supplier, Consumer<IOSideConfig> consumer) {
        return new IOSideConfigSyncable() {
            @Override
            public IOSideConfig get() {
                return supplier.get();
            }

            @Override
            public void set(IOSideConfig value) {
                consumer.accept(value);
            }
        };
    }
}
