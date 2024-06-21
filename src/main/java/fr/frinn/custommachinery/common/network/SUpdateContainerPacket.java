package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SUpdateContainerPacket(int windowId, List<IData<?>> data) implements CustomPacketPayload {

    public static final Type<SUpdateContainerPacket> TYPE = new Type<>(CustomMachinery.rl("update_container"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SUpdateContainerPacket> CODEC = StreamCodec.ofMember(SUpdateContainerPacket::write, SUpdateContainerPacket::read);

    @Override
    public Type<SUpdateContainerPacket> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.windowId);
        buf.writeShort(this.data.size());
        this.data.forEach(data -> data.writeData(buf));
    }

    public static SUpdateContainerPacket read(RegistryFriendlyByteBuf buf) {
        int windowId = buf.readInt();
        List<IData<?>> dataList = new ArrayList<>();
        short size = buf.readShort();
        for(short i = 0; i < size; i++) {
            IData<?> data = IData.readData(buf);
            if(data != null)
                dataList.add(data);
        }
        return new SUpdateContainerPacket(windowId, dataList);
    }

    public static void handle(SUpdateContainerPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> ClientPacketHandler.handleUpdateContainerPacket(packet.windowId, packet.data));
    }
}
