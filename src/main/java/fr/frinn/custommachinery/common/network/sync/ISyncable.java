package fr.frinn.custommachinery.common.network.sync;

import fr.frinn.custommachinery.common.network.sync.data.IData;

public interface ISyncable<D extends IData, T> {

    T get();

    void set(T value);

    boolean needSync();

    D getData(short id);
}
