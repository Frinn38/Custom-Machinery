package fr.frinn.custommachinery.common.network.sync;

import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.api.network.ISyncable;

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
