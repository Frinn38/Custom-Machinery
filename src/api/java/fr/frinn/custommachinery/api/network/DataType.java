package fr.frinn.custommachinery.api.network;

import fr.frinn.custommachinery.api.CustomMachineryAPI;
import fr.frinn.custommachinery.apiimpl.network.data.BooleanData;
import fr.frinn.custommachinery.apiimpl.network.data.DoubleData;
import fr.frinn.custommachinery.apiimpl.network.data.FluidStackData;
import fr.frinn.custommachinery.apiimpl.network.data.IntegerData;
import fr.frinn.custommachinery.apiimpl.network.data.ItemStackData;
import fr.frinn.custommachinery.apiimpl.network.data.LongData;
import fr.frinn.custommachinery.apiimpl.network.data.StringData;
import fr.frinn.custommachinery.apiimpl.network.syncable.BooleanSyncable;
import fr.frinn.custommachinery.apiimpl.network.syncable.DoubleSyncable;
import fr.frinn.custommachinery.apiimpl.network.syncable.FluidStackSyncable;
import fr.frinn.custommachinery.apiimpl.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.apiimpl.network.syncable.ItemStackSyncable;
import fr.frinn.custommachinery.apiimpl.network.syncable.LongSyncable;
import fr.frinn.custommachinery.apiimpl.network.syncable.StringSyncable;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Used by the container syncing packet to create the proper IData Object from the PacketBuffer send by the server.
 */
public class DataType<D extends IData<T>, T> extends ForgeRegistryEntry<DataType<? extends IData<?>, ?>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final DeferredRegister<DataType<?, ?>> DATA = DeferredRegister.create((Class)DataType.class, CustomMachineryAPI.MODID);
    public static final Supplier<IForgeRegistry<DataType<?, ?>>> DATA_REGISTRY = DATA.makeRegistry("data_type", RegistryBuilder::new);
    public static final RegistryObject<DataType<BooleanData, Boolean>> BOOLEAN_DATA = DATA.register("boolean", () -> new DataType<>(BooleanSyncable::create, BooleanData::new));
    public static final RegistryObject<DataType<IntegerData, Integer>> INTEGER_DATA = DATA.register("integer", () -> new DataType<>(IntegerSyncable::create, IntegerData::new));
    public static final RegistryObject<DataType<DoubleData, Double>> DOUBLE_DATA = DATA.register("double", () -> new DataType<>(DoubleSyncable::create, DoubleData::new));
    public static final RegistryObject<DataType<ItemStackData, ItemStack>> ITEMSTACK_DATA = DATA.register("itemstack", () -> new DataType<>(ItemStackSyncable::create, ItemStackData::new));
    public static final RegistryObject<DataType<FluidStackData, FluidStack>> FLUIDSTACK_DATA = DATA.register("fluidstack", () -> new DataType<>(FluidStackSyncable::create, FluidStackData::new));
    public static final RegistryObject<DataType<StringData, String>> STRING_DATA = DATA.register("string", () -> new DataType<>(StringSyncable::create, StringData::new));
    public static final RegistryObject<DataType<LongData, Long>> LONG_DATA = DATA.register("long", () -> new DataType<>(LongSyncable::create, LongData::new));

    private final BiFunction<Supplier<T>, Consumer<T>, ISyncable<D, T>> builder;
    private final BiFunction<Short, PacketBuffer, D> reader;

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
