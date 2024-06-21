package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SOpenEditScreenPacket(ResourceLocation machine) implements CustomPacketPayload {

    public static final Type<SOpenEditScreenPacket> TYPE = new Type<>(CustomMachinery.rl("open_edit_screen"));

    public static final StreamCodec<ByteBuf, SOpenEditScreenPacket> CODEC = ResourceLocation.STREAM_CODEC.map(SOpenEditScreenPacket::new, SOpenEditScreenPacket::machine);

    @Override
    public Type<SOpenEditScreenPacket> type() {
        return TYPE;
    }

    public static void handle(SOpenEditScreenPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            ClientPacketHandler.handleOpenEditScreenPacket(packet.machine);
    }
}
