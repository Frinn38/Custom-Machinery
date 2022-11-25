package fr.frinn.custommachinery.impl.network;

import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.api.network.ISyncable;

/**
 * Default implementation of ISyncable.
 */
public abstract class AbstractSyncable<D extends IData<?>, T> implements ISyncable<D, T> {

    public T lastKnownValue;

    /**
     * Ensure that the data is synced only if the hold value changed.
     * @return true if the data need to be synced.
     */
    @Override
    public boolean needSync() {
        T value = get();
        boolean needSync = !value.equals(this.lastKnownValue);
        this.lastKnownValue = value;
        return needSync;
    }
}
