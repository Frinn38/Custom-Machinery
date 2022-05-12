package fr.frinn.custommachinery.api.network;

import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Used by the container syncing packet to create the proper IData Object from the PacketBuffer send by the server.
 */
public class DataType<D extends IData<T>, T> extends ForgeRegistryEntry<DataType<? extends IData<?>, ?>> {

    public static final ResourceKey<Registry<DataType<?, ?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("data_type"));

    private final Class<T> type;
    private final BiFunction<Supplier<T>, Consumer<T>, ISyncable<D, T>> builder;
    private final BiFunction<Short, FriendlyByteBuf, D> reader;

    public DataType(Class<T> type, BiFunction<Supplier<T>, Consumer<T>, ISyncable<D, T>> builder, BiFunction<Short, FriendlyByteBuf, D> reader) {
        this.type = type;
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
    public D readData(short id, FriendlyByteBuf buffer) {
        return this.reader.apply(id, buffer);
    }

    /**
     * @param type The class of the object to sync, used to retrieve the correct DataType from the registry.
     * @param supplier A supplier, used to get the synced object on server side.
     * @param consumer A consumer, used to set the synced object on client side.
     * @return An {@link ISyncable} for the specified object class.
     */
    @SuppressWarnings("unchecked")
    public static <T> ISyncable<IData<T>, T> createSyncable(Class<T> type, Supplier<T> supplier, Consumer<T> consumer) {
        Optional<DataType<IData<T>, T>> dataType = ICustomMachineryAPI.INSTANCE.dataRegistry().getValues().stream().filter(entry -> entry.type == type).map(entry -> (DataType<IData<T>, T>)entry).findFirst();
        if(dataType.isPresent())
            return dataType.get().createSyncable(supplier, consumer);
        throw new IllegalArgumentException("Couldn't create Syncable for provided type: " + type.getName() + ". No registered DataType for this type.");
    }
}
