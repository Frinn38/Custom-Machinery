package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;

public class SOpenCreationScreenPacket extends BaseS2CMessage {

    @Override
    public MessageType getType() {
        return PacketManager.OPEN_CREATION_SCREEN;
    }

    public static SOpenCreationScreenPacket read(FriendlyByteBuf buf) {
        return new SOpenCreationScreenPacket();
    }

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public void handle(PacketContext context) {
        if(context.getEnvironment() == Env.CLIENT)
            ClientPacketHandler.handleOpenCreationScreenPacket();
    }
}
