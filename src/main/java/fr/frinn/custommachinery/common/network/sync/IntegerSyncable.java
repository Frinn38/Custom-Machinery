package fr.frinn.custommachinery.common.network.sync;

import fr.frinn.custommachinery.common.network.sync.data.IntegerData;
import fr.frinn.custommachinery.impl.network.AbstractSyncable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class IntegerSyncable extends AbstractSyncable<IntegerData, Integer> {

    @Override
    public IntegerData getData(short id) {
        return new IntegerData(id, get());
    }

    public static IntegerSyncable create(Supplier<Integer> supplier, Consumer<Integer> consumer) {
        return new IntegerSyncable() {
            @Override
            public Integer get() {
                return supplier.get();
            }

            @Override
            public void set(Integer value) {
                consumer.accept(value);
            }
        };
    }
}
