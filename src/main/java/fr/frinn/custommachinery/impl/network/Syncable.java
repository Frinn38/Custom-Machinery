package fr.frinn.custommachinery.impl.network;

import fr.frinn.custommachinery.api.network.DataType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Syncable<T> {

    private final DataType<T> type;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    @Nullable
    public T lastKnownValue;

    public Syncable(DataType<T> type, Supplier<T> getter, Consumer<T> setter) {
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    public T get() {
        return this.getter.get();
    }

    public void set(T value) {
        this.setter.accept(value);
    }

    public Data<T> makeData(short id) {
        return new Data<>(this.type, id, this.get());
    }

    /**
     * Ensure that the data is synced only if the hold value changed.
     * @return true if the data need to be synced.
     */
    public boolean needSync() {
        T value = this.get();
        boolean needSync;
        if(this.lastKnownValue != null)
            needSync = !this.type.areEquals(this.lastKnownValue, value);
        else
            needSync = true;
        this.lastKnownValue = this.type.copyValue(value);
        return needSync;
    }
}
