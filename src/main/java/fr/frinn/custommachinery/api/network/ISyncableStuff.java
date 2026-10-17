package fr.frinn.custommachinery.api.network;

import fr.frinn.custommachinery.impl.network.SyncableList;

/**
 * Used to pass some ISyncable to the MachineTile syncing system.
 * IMachineComponent implementing this interface will be able to sync some data from server to client side automatically.
 * The data will be synced as needed by the system.
 */
public interface ISyncableStuff {

    /**
     * This will be called on both logical sides when a MachineTile container is open by a player.
     * The order of ISyncable passed to the container must be the same on both sides.
     * You can create an ISyncable instance using <pre>{@code DataType.createSyncable(T.class, Supplier<T>, Consumer<T>)}</pre>
     * Or by using one of the methods in the given {@link SyncableList}.
     * @param list A {@link SyncableList} used to pass all ISyncable you want to be synced.
     */
    void getStuffToSync(SyncableList list);
}
