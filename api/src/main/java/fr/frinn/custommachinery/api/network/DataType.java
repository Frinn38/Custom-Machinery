package fr.frinn.custommachinery.api.network;

import dev.architectury.core.RegistryEntry;
import dev.architectury.registry.registries.DeferredRegister;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Used by the machine container to sync some data of a specific type T.
 * A {@link RegistryEntry} used for registering custom {@link DataType}.
 * All instances of this class must be created and registered using {@link Registry} for Fabric or {@link DeferredRegister} for Forge or Architectury.
 * @param <T> The {@link IData} handled by this {@link DataType}.
 */
public class DataType<D extends IData<T>, T> extends RegistryEntry<DataType<D, T>> {

    /**
     * The {@link ResourceKey} pointing to the {@link DataType} vanilla registry.
     * Can be used to create a {@link DeferredRegister} for registering your {@link DataType}.
     */
    public static final ResourceKey<Registry<DataType<?, ?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("data_type"));

    /**
     * A factory method to create new {@link DataType}.
     * @param type The {@link Class} of the object to sync.
     * @param builder A {@link BiFunction} that will build the {@link ISyncable} instance from a getter and a setter.
     * @param reader A {@link BiFunction} that retrieve the object to sync from vanilla {@link FriendlyByteBuf}.
     * @param <T> The type of the object to sync.
     * @param <D> The {@link IData} used to sync the object.
     */
    public static <T, D extends IData<T>> DataType<D, T> create(Class<T> type, BiFunction<Supplier<T>, Consumer<T>, ISyncable<D, T>> builder, BiFunction<Short, FriendlyByteBuf, D> reader) {
        return new DataType<>(type, builder, reader);
    }

    private final Class<T> type;
    private final BiFunction<Supplier<T>, Consumer<T>, ISyncable<D, T>> builder;
    private final BiFunction<Short, FriendlyByteBuf, D> reader;

    /**
     * A constructor for {@link DataType}.
     * Use {@link DataType#create(Class, BiFunction, BiFunction)} instead.
     */
    private DataType(Class<T> type, BiFunction<Supplier<T>, Consumer<T>, ISyncable<D, T>> builder, BiFunction<Short, FriendlyByteBuf, D> reader) {
        this.type = type;
        this.builder = builder;
        this.reader = reader;
    }

    /**
     * This can be used by addons to create {@link ISyncable} instance without directly referencing the class implementing {@link ISyncable}.
     * @param supplier A {@link Supplier}, used to get the synced object on server side.
     * @param consumer A {@link Consumer}, used to set the synced object on client side.
     * @return An instance of {@link ISyncable} that can be passed to the container using {@link ISyncableStuff#getStuffToSync(Consumer)} method.
     */
    public ISyncable<D, T> createSyncable(Supplier<T> supplier, Consumer<T> consumer) {
        return this.builder.apply(supplier, consumer);
    }

    /**
     * Used to create an {@link IData} instance for this type, using the {@link FriendlyByteBuf} sent by the server.
     * @param id The {@link IData} ID, previously read by {@link IData#readData(FriendlyByteBuf)}.
     * @param buffer The {@link FriendlyByteBuf} sent by the server.
     * @return An {@link IData} of this type, holding the synced object.
     */
    public D readData(short id, FriendlyByteBuf buffer) {
        return this.reader.apply(id, buffer);
    }

    /**
     * @param type The {@link Class} of the object to sync, used to retrieve the correct {@link DataType} from the registry.
     * @param supplier A {@link Supplier}, used to get the synced object on server side.
     * @param consumer A {@link Consumer}, used to set the synced object on client side.
     * @return An {@link ISyncable} for the specified object class.
     */
    @SuppressWarnings("unchecked")
    public static <T> ISyncable<IData<T>, T> createSyncable(Class<T> type, Supplier<T> supplier, Consumer<T> consumer) {
        Optional<DataType<IData<T>, T>> dataType = ICustomMachineryAPI.INSTANCE.dataRegistrar().entrySet().stream().filter(entry -> entry.getValue().type == type).map(entry -> (DataType<IData<T>, T>)entry).findFirst();
        if(dataType.isPresent())
            return dataType.get().createSyncable(supplier, consumer);
        throw new IllegalArgumentException("Couldn't create Syncable for provided type: " + type.getName() + ". No registered DataType for this type.");
    }

    /**
     * A helper method to get the ID of this {@link DataType}.
     * @return The ID of this {@link DataType}, or null if it is not registered.
     */
    public ResourceLocation getId() {
        return ICustomMachineryAPI.INSTANCE.dataRegistrar().getId(this);
    }
}
