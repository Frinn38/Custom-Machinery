package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SRefreshCustomMachineTilePacket(BlockPos pos, ResourceLocation machine) implements CustomPacketPayload {

    public static final Type<SRefreshCustomMachineTilePacket> TYPE = new Type<>(CustomMachinery.rl("refresh_machine_tile"));

    public static final StreamCodec<ByteBuf, SRefreshCustomMachineTilePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SRefreshCustomMachineTilePacket::pos,
            ResourceLocation.STREAM_CODEC,
            SRefreshCustomMachineTilePacket::machine,
            SRefreshCustomMachineTilePacket::new
    );

    @Override
    public Type<SRefreshCustomMachineTilePacket> type() {
        return TYPE;
    }

    public static void handle(SRefreshCustomMachineTilePacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> ClientPacketHandler.handleRefreshCustomMachineTilePacket(packet.pos, packet.machine));
    }
}
