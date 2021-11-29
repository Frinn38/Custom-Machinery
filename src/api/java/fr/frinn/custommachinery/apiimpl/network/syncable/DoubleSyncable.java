package fr.frinn.custommachinery.apiimpl.network.syncable;

import fr.frinn.custommachinery.apiimpl.network.data.DoubleData;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class DoubleSyncable extends AbstractSyncable<DoubleData, Double> {

    @Override
    public DoubleData getData(short id) {
        return new DoubleData(id, get());
    }

    public static DoubleSyncable create(Supplier<Double> supplier, Consumer<Double> consumer) {
        return new DoubleSyncable() {
            @Override
            public Double get() {
                return supplier.get();
            }

            @Override
            public void set(Double value) {
                consumer.accept(value);
            }
        };
    }
}
