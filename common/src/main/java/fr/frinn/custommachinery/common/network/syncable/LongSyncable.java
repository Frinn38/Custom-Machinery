package fr.frinn.custommachinery.common.network.syncable;

import fr.frinn.custommachinery.common.network.data.LongData;
import fr.frinn.custommachinery.impl.network.AbstractSyncable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class LongSyncable extends AbstractSyncable<LongData, Long> {

    @Override
    public LongData getData(short id) {
        return new LongData(id, get());
    }

    public static LongSyncable create(Supplier<Long> supplier, Consumer<Long> consumer) {
        return new LongSyncable() {
            @Override
            public Long get() {
                return supplier.get();
            }

            @Override
            public void set(Long value) {
                consumer.accept(value);
            }
        };
    }
}
