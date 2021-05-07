package fr.frinn.custommachinery.common.network.sync.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.sync.ISyncable;
import net.minecraft.network.PacketBuffer;

public interface IData<T> {

    DataType<?, T> getType();

    short getID();

    T getValue();

    default void writeData(PacketBuffer buffer) {
        buffer.writeResourceLocation(getType().getRegistryName());
        buffer.writeShort(getID());
    }

    static IData<?> readData(PacketBuffer buffer) {
        DataType<?, ?> type = Registration.DATA_REGISTRY.get().getValue(buffer.readResourceLocation());
        short id = buffer.readShort();
        return type.readData(id, buffer);
    }

    default void handleData(ISyncable<?, T> syncable) {
        syncable.set(getValue());
    }
}
