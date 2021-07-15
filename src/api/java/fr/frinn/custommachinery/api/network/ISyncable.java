package fr.frinn.custommachinery.api.network;

public interface ISyncable<D extends IData, T> {

    T get();

    void set(T value);

    boolean needSync();

    D getData(short id);
}
