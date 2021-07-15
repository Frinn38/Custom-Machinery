package fr.frinn.custommachinery.api.network;

import fr.frinn.custommachinery.api.CustomMachineryAPI;
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
        DataType<?, ?> type = CustomMachineryAPI.getDataRegistry().getValue(buffer.readResourceLocation());
        short id = buffer.readShort();
        return type.readData(id, buffer);
    }

    default void handleData(ISyncable<?, T> syncable) {
        syncable.set(getValue());
    }
}
