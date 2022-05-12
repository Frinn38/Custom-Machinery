package fr.frinn.custommachinery.common.network;

import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SOpenFilePacket {

    private String path;

    public SOpenFilePacket(String path) {
        this.path = path;
    }

    public static void encode(SOpenFilePacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.path);
    }

    public static SOpenFilePacket decode(FriendlyByteBuf buf) {
        return new SOpenFilePacket(buf.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
            context.get().enqueueWork(() -> Util.getPlatform().openUri(this.path));
        context.get().setPacketHandled(true);
    }
}
