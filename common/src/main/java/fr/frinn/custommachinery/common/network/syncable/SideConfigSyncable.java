package fr.frinn.custommachinery.common.network.syncable;

import fr.frinn.custommachinery.common.network.data.SideConfigData;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class SideConfigSyncable extends AbstractSyncable<SideConfigData, SideConfig> {

    @Override
    public SideConfigData getData(short id) {
        return new SideConfigData(id, get());
    }

    @Override
    public boolean needSync() {
        SideConfig value = get();
        if(this.lastKnownValue == null) {
            this.lastKnownValue = value.copy();
            return true;
        }
        for(RelativeSide side : RelativeSide.values()) {
            if(this.lastKnownValue.getSideMode(side) != value.getSideMode(side)) {
                this.lastKnownValue = value.copy();
                return true;
            }
        }
        if(this.lastKnownValue.isAutoInput() != value.isAutoInput() || this.lastKnownValue.isAutoOutput() != value.isAutoOutput()) {
            this.lastKnownValue = value.copy();
            return true;
        }
        return false;
    }

    public static SideConfigSyncable create(Supplier<SideConfig> supplier, Consumer<SideConfig> consumer) {
        return new SideConfigSyncable() {
            @Override
            public SideConfig get() {
                return supplier.get();
            }

            @Override
            public void set(SideConfig value) {
                consumer.accept(value);
            }
        };
    }
}
