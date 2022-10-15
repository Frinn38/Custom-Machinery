package fr.frinn.custommachinery.api.network;

/**
 * Used to automatically sync any kind of object from server logical side to client.
 * @param <D> A Data, used for encoding/decoding the data in the packet.
 * @param <T> The type of Object to sync.
 */
public interface ISyncable<D extends IData<?>, T> {

    /**
     * @return The value of the Object hold by this ISyncable.
     */
    T get();

    /**
     * @param value Set the value of the Object hold by this ISyncable.
     */
    void set(T value);

    /**
     * Called each tick on server side for each player that opened a MachineTile container.
     * @return True if the Object hold by this ISyncable has changed and need syncing.
     */
    boolean needSync();

    /**
     * @param id The id of this IData, used by the syncing packet to encode/decode the data in the proper order.
     *           This id is unique to each IData, meaning that there can't be more than 256 IData synced by the same container.
     * @return The IData used to sync the Object hold by this ISyncable.
     */
    D getData(short id);
}
