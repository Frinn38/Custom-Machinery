package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SOpenCreationScreenPacket() implements CustomPacketPayload {

    public static final Type<SOpenCreationScreenPacket> TYPE = new Type<>(CustomMachinery.rl("open_creation_screen"));

    public static final StreamCodec<ByteBuf, SOpenCreationScreenPacket> CODEC = StreamCodec.unit(new SOpenCreationScreenPacket());

    @Override
    public Type<SOpenCreationScreenPacket> type() {
        return TYPE;
    }

    public static void handle(SOpenCreationScreenPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            ClientPacketHandler.handleOpenCreationScreenPacket();
    }
}
