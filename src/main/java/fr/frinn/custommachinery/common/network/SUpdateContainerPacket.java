package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SUpdateContainerPacket(int windowId, List<Data<?>> data) implements CustomPacketPayload {

    public static final Type<SUpdateContainerPacket> TYPE = new Type<>(CustomMachinery.rl("update_container"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SUpdateContainerPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SUpdateContainerPacket::windowId,
            Data.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SUpdateContainerPacket::data,
            SUpdateContainerPacket::new
    );

    @Override
    public Type<SUpdateContainerPacket> type() {
        return TYPE;
    }

    public static void handle(SUpdateContainerPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> ClientPacketHandler.handleUpdateContainerPacket(packet.windowId, packet.data));
    }
}
