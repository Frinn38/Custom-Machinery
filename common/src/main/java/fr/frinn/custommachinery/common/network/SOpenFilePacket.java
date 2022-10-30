package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;

public class SOpenFilePacket extends BaseS2CMessage {

    private final String path;

    public SOpenFilePacket(String path) {
        this.path = path;
    }

    @Override
    public MessageType getType() {
        return PacketManager.OPEN_FILE;
    }

    @Override
    public  void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.path);
    }

    public static SOpenFilePacket decode(FriendlyByteBuf buf) {
        return new SOpenFilePacket(buf.readUtf());
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnvironment() == Env.CLIENT)
            context.queue(() -> Util.getPlatform().openUri(this.path));
    }
}
