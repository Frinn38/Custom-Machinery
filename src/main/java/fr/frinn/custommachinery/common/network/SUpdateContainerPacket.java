package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SUpdateContainerPacket {

    private final int windowId;
    private final List<IData<?>> data;

    public SUpdateContainerPacket(int windowId, List<IData<?>> data) {
        this.data = data;
        this.windowId = windowId;
    }

    public static void encode(SUpdateContainerPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.windowId);
        buf.writeShort(pkt.data.size());
        pkt.data.forEach(data -> data.writeData(buf));
    }

    public static SUpdateContainerPacket decode(FriendlyByteBuf buf) {
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

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
            context.get().enqueueWork(() -> ClientPacketHandler.handleUpdateContainerPacket(this.windowId, this.data));
        context.get().setPacketHandled(true);
    }
}
