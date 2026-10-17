package fr.frinn.custommachinery.api.network;

import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.network.Data;
import fr.frinn.custommachinery.impl.network.Syncable;
import fr.frinn.custommachinery.impl.network.SyncableList;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Used by the machine container to sync some data of a specific type T.
 * Used for registering custom {@link DataType}.
 * All instances of this class must be created and registered using {@link net.neoforged.neoforge.registries.DeferredRegister}.
 * @param <T> The data handled by this {@link DataType}.
 */
public class DataType<T> {

    /**
     * The {@link ResourceKey} pointing to the {@link DataType} vanilla registry.
     * Can be used to create a {@link net.neoforged.neoforge.registries.DeferredRegister} for registering your {@link DataType}.
     */
    public static final ResourceKey<Registry<DataType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("data_type"));

    /**
     * A factory method to create new {@link DataType}.
     * @param type The {@link Class} of the object to sync.
     * @param streamCodec A stream codec that will be used to send and receive the data over the network.
     * @param <T> The type of the object to sync.
     */
    public static <T> DataType<T> create(Class<T> type, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return new DataType<>(type, streamCodec, null, null);
    }

    /**
     * A factory method to create new {@link DataType}.
     * @param type The {@link Class} of the object to sync.
     * @param streamCodec A stream codec that will be used to send and receive the data over the network.
     * @param equalityCheck A predicate that should return true when both data are equals.
     *                      Note that the last data might be null if the {@link Syncable} holding it was just initialized.
     * @param <T> The type of the object to sync.
     */
    public static <T> DataType<T> create(Class<T> type, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, BiPredicate<T, T> equalityCheck) {
        return new DataType<>(type, streamCodec, equalityCheck, null);
    }

    /**
     * A factory method to create new {@link DataType}.
     * @param type The {@link Class} of the object to sync.
     * @param streamCodec A stream codec that will be used to send and receive the data over the network.
     * @param copier An operator that should return a copy of the passed object.
     * @param <T> The type of the object to sync.
     */
    public static <T> DataType<T> create(Class<T> type, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, UnaryOperator<T> copier) {
        return new DataType<>(type, streamCodec, null, copier);
    }

    /**
     * A factory method to create new {@link DataType}.
     * @param type The {@link Class} of the object to sync.
     * @param streamCodec A stream codec that will be used to send and receive the data over the network.
     * @param equalityCheck A predicate that should return true when both data are equals.
     *                      Note that the last data might be null if the {@link Syncable} holding it was just initialized.
     * @param copier An operator that should return a copy of the passed object.
     * @param <T> The type of the object to sync.
     */
    public static <T> DataType<T> create(Class<T> type, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, BiPredicate<T, T> equalityCheck, UnaryOperator<T> copier) {
        return new DataType<>(type, streamCodec, equalityCheck, null);
    }

    private final Class<T> type;
    private final StreamCodec<RegistryFriendlyByteBuf, Data<T>> streamCodec;
    @Nullable
    private final BiPredicate<T, T> equalityCheck;
    @Nullable
    private final UnaryOperator<T> copier;

    /**
     * A constructor for {@link DataType}.
     * Use {@link DataType#create(Class, StreamCodec)} instead.
     */
    private DataType(Class<T> type, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, @Nullable BiPredicate<T, T> equalityCheck, @Nullable UnaryOperator<T> copier) {
        this.type = type;
        this.streamCodec = StreamCodec.composite(
                ByteBufCodecs.registry(Utils.cast(REGISTRY_KEY)),
                Data::type,
                ByteBufCodecs.SHORT,
                Data::id,
                streamCodec,
                Data::value,
                Data::new
        );
        this.equalityCheck = equalityCheck;
        this.copier = copier;
    }

    /**
     * This can be used to create {@link Syncable} instance for this type.
     * @param getter A {@link Supplier}, used to get the synced object on server side.
     * @param setter A {@link Consumer}, used to set the synced object on client side.
     * @return An instance of {@link Syncable} that can be passed to the container using {@link ISyncableStuff#getStuffToSync(SyncableList)} method.
     */
    public Syncable<T> createSyncable(Supplier<T> getter, Consumer<T> setter) {
        return new Syncable<>(this, getter, setter);
    }

    /**
     * This will be used to get the {@link StreamCodec} used to send the data to the client.
     * @return A {@link StreamCodec} that can encode and decode this type of data.
     */
    public StreamCodec<RegistryFriendlyByteBuf, Data<T>> streamCodec() {
        return this.streamCodec;
    }

    /**
     * Compare the equality of the last data and the current data to determine if the new data must be sent to clients.
     * @param lastValue The last checked data.
     * @param newValue The current data.
     * @return True if both data are equals, false otherwise.
     */
    public boolean areEquals(T lastValue, T newValue) {
        if(this.equalityCheck == null)
            return Objects.equals(lastValue, newValue);
        return this.equalityCheck.test(lastValue, newValue);
    }

    /**
     * Create a copy of the current data, to store it and be sure it will never be modified if the current data is modified.
     * @param value The current data.
     * @return A copy if the current data.
     */
    public T copyValue(T value) {
        if(this.copier == null)
            return value;
        return this.copier.apply(value);
    }

    /**
     * @param type The {@link Class} of the object to sync, used to retrieve the correct {@link DataType} from the registry.
     * @param supplier A {@link Supplier}, used to get the synced object on server side.
     * @param consumer A {@link Consumer}, used to set the synced object on client side.
     * @return An {@link Syncable} for the specified object class.
     */
    @SuppressWarnings("unchecked")
    public static <T> Syncable<T> createSyncable(Class<T> type, Supplier<T> supplier, Consumer<T> consumer) {
        Optional<DataType<T>> dataType = ICustomMachineryAPI.INSTANCE.dataRegistrar().entrySet().stream().filter(entry -> entry.getValue().type == type).map(entry -> (DataType<T>)entry.getValue()).findFirst();
        if(dataType.isPresent())
            return dataType.get().createSyncable(supplier, consumer);
        throw new IllegalArgumentException("Couldn't create Syncable for provided type: " + type.getName() + ". No registered DataType for this type.");
    }

    /**
     * A helper method to get the ID of this {@link DataType}.
     * @return The ID of this {@link DataType}.
     */
    public Identifier getId() {
        Identifier id = ICustomMachineryAPI.INSTANCE.dataRegistrar().getKey(this);
        if(id == null)
            throw new IllegalStateException("Trying to get id for an unregistered data type");
        return id;
    }
}
