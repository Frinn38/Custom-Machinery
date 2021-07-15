package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.common.network.sync.SyncableContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SUpdateContainerPacket {

    private int windowId;
    private List<IData<?>> data;

    public SUpdateContainerPacket(int windowId, List<IData<?>> data) {
        this.data = data;
        this.windowId = windowId;
    }

    public static void encode(SUpdateContainerPacket pkt, PacketBuffer buf) {
        buf.writeInt(pkt.windowId);
        buf.writeShort(pkt.data.size());
        pkt.data.forEach(data -> data.writeData(buf));
    }

    public static SUpdateContainerPacket decode(PacketBuffer buf) {
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
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> {
                ClientPlayerEntity player = Minecraft.getInstance().player;
                if(player != null && player.openContainer instanceof SyncableContainer && player.openContainer.windowId == this.windowId) {
                    SyncableContainer container = (SyncableContainer)player.openContainer;
                    this.data.forEach(container::handleData);
                }

            });
        }
        context.get().setPacketHandled(true);
    }
}
