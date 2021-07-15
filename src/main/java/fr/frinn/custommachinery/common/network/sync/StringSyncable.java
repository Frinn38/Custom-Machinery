package fr.frinn.custommachinery.common.network.sync;

import fr.frinn.custommachinery.api.network.AbstractSyncable;
import fr.frinn.custommachinery.common.network.sync.data.StringData;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class StringSyncable extends AbstractSyncable<StringData, String> {

    @Override
    public StringData getData(short id) {
        return new StringData(id, get());
    }

    @Override
    public boolean needSync() {
        String value = get();
        boolean needSync = !value.equals(this.lastKnownValue);
        this.lastKnownValue = value;
        return needSync;
    }

    public static StringSyncable create(Supplier<String> supplier, Consumer<String> consumer) {
        return new StringSyncable() {
            @Override
            public String get() {
                return supplier.get();
            }

            @Override
            public void set(String value) {
                consumer.accept(value);
            }
        };
    }
}
