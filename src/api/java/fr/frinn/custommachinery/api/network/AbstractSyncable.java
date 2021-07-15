package fr.frinn.custommachinery.api.network;

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
        boolean needSync = value != this.lastKnownValue;
        this.lastKnownValue = value;
        return needSync;
    }
}
