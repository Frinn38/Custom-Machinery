package fr.frinn.custommachinery.common.network.sync;

import fr.frinn.custommachinery.common.network.sync.data.IData;

public abstract class AbstractSyncable<D extends IData, T> implements ISyncable<D, T> {

    public T lastKnownValue;

    @Override
    public boolean needSync() {
        T value = get();
        boolean needSync = value != this.lastKnownValue;
        this.lastKnownValue = value;
        return needSync;
    }
}
