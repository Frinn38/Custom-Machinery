package fr.frinn.custommachinery.common.network.sync;

import fr.frinn.custommachinery.common.network.sync.data.BooleanData;
import fr.frinn.custommachinery.impl.network.AbstractSyncable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BooleanSyncable extends AbstractSyncable<BooleanData, Boolean> {

    @Override
    public BooleanData getData(short id) {
        return new BooleanData(id, get());
    }

    public static BooleanSyncable create(Supplier<Boolean> supplier, Consumer<Boolean> consumer) {
        return new BooleanSyncable() {
            @Override
            public Boolean get() {
                return supplier.get();
            }

            @Override
            public void set(Boolean value) {
                consumer.accept(value);
            }
        };
    }
}
