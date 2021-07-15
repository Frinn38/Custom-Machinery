package fr.frinn.custommachinery.api.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Used by the container syncing packet to create the proper IData Object from the PacketBuffer send by the server.
 */
public class DataType<D extends IData<T>, T> extends ForgeRegistryEntry<DataType<? extends IData<?>, ?>> {

    private BiFunction<Supplier<T>, Consumer<T>, ISyncable<D, T>> builder;
    private BiFunction<Short, PacketBuffer, D> reader;

    public DataType(BiFunction<Supplier<T>, Consumer<T>, ISyncable<D, T>> builder, BiFunction<Short, PacketBuffer, D> reader) {
        this.builder = builder;
        this.reader = reader;
    }

    /**
     * This can be used by addons to create ISyncable Object without directly referencing the class implementing ISyncable.
     * @param supplier A supplier, used to get the synced object on server side.
     * @param consumer A consumer, used to set the synced object on client side.
     * @return An instance of ISyncable that can be passed to the container using ISyncableStuff#getStuffToSync method.
     */
    public ISyncable<D, T> createSyncable(Supplier<T> supplier, Consumer<T> consumer) {
        return this.builder.apply(supplier, consumer);
    }

    /**
     * Used to create an IData instance for this type, using the PacketBuffer send by the server.
     * @param id The IData ID, previously read by IData.readData()
     * @param buffer The PacketBuffer send by the server.
     * @return An IData of this type, holding the synced Object.
     */
    public D readData(short id, PacketBuffer buffer) {
        return this.reader.apply(id, buffer);
    }
}
