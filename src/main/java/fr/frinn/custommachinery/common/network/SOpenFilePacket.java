package fr.frinn.custommachinery.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SOpenFilePacket {

    private String path;

    public SOpenFilePacket(String path) {
        this.path = path;
    }

    public static void encode(SOpenFilePacket pkt, PacketBuffer buf) {
        buf.writeString(pkt.path);
    }

    public static SOpenFilePacket decode(PacketBuffer buf) {
        return new SOpenFilePacket(buf.readString());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Util.getOSType().openURI(this.path);
        });
        context.get().setPacketHandled(true);
    }
}
