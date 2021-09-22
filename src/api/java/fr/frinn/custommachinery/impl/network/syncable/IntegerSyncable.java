package fr.frinn.custommachinery.impl.network.syncable;

import fr.frinn.custommachinery.impl.network.data.IntegerData;

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
