package fr.frinn.custommachinery.api.network;

import fr.frinn.custommachinery.api.CustomMachineryAPI;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Used to sync any kind of Object from server logical side to client.
 * @param <T> The Object to sync.
 */
public interface IData<T> {

    /**
     * @return A registered DataType corresponding to this IData.
     */
    DataType<?, T> getType();

    /**
     * @return The ID of this IData, used by the container syncing packet to read/write the data in the proper order.
     */
    short getID();

    /**
     * @return The value of the object hold by this IData.
     */
    T getValue();

    /**
     * Override this method to pass write the Object hold by this IData to the PacketBuffer.
     * Overriding methods MUST call super.writeData() BEFORE writing their stuff into the PacketBuffer.
     * @param buffer The PacketBuffer that will be send to the client.
     */
    default void writeData(PacketBuffer buffer) {
        if(getType().getRegistryName() == null)
            throw new IllegalStateException("Attempting to write invalid data to Custom Machine container syncing packet : " + getType().toString() + " is not registered !");
        buffer.writeResourceLocation(getType().getRegistryName());
        buffer.writeShort(getID());
    }

    /**
     * Utility method used by the container syncing packet to construct the IData on client side, from the PacketBuffer send by the server.
     * Don't touch this.
     */
    static IData<?> readData(PacketBuffer buffer) {
        ResourceLocation typeId = buffer.readResourceLocation();
        DataType<?, ?> type = CustomMachineryAPI.DATA_REGISTRY.getValue(typeId);
        if(type == null)
            throw new IllegalStateException("Attempting to read invalid IData : " + typeId + " is not a valid registered DataType !");
        short id = buffer.readShort();
        return type.readData(id, buffer);
    }
}
