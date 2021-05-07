package fr.frinn.custommachinery.common.network.sync.data;

import fr.frinn.custommachinery.common.network.sync.ISyncable;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DataType<D extends IData, T> extends ForgeRegistryEntry<DataType<?, ?>> {

    private BiFunction<Supplier<T>, Consumer<T>, ISyncable<D, T>> builder;
    private BiFunction<Short, PacketBuffer, D> reader;

    public DataType(BiFunction<Supplier<T>, Consumer<T>, ISyncable<D, T>> builder, BiFunction<Short, PacketBuffer, D> reader) {
        this.builder = builder;
        this.reader = reader;
    }

    public ISyncable<D, T> createSyncable(Supplier<T> supplier, Consumer<T> consumer) {
        return this.builder.apply(supplier, consumer);
    }

    public D readData(short id, PacketBuffer buffer) {
        return this.reader.apply(id, buffer);
    }
}
