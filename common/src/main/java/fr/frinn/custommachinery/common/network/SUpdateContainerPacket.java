package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class SUpdateContainerPacket extends BaseS2CMessage {

    private final int windowId;
    private final List<IData<?>> data;

    public SUpdateContainerPacket(int windowId, List<IData<?>> data) {
        this.data = data;
        this.windowId = windowId;
    }

    @Override
    public MessageType getType() {
        return PacketManager.UPDATE_CONSTAINER;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.windowId);
        buf.writeShort(this.data.size());
        this.data.forEach(data -> data.writeData(buf));
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

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnv() == EnvType.CLIENT)
            context.queue(() -> ClientPacketHandler.handleUpdateContainerPacket(this.windowId, this.data));
    }
}
