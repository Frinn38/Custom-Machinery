package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SMachineCoreCountChangePacket(BlockPos pos, int count) implements CustomPacketPayload {

    public static final Type<SMachineCoreCountChangePacket> TYPE = new Type<>(CustomMachinery.rl("machine_core_count_change"));

    public static final StreamCodec<ByteBuf, SMachineCoreCountChangePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SMachineCoreCountChangePacket::pos,
            ByteBufCodecs.VAR_INT,
            SMachineCoreCountChangePacket::count,
            SMachineCoreCountChangePacket::new
    );

    @Override
    public Type<SMachineCoreCountChangePacket> type() {
        return TYPE;
    }

    public static void handle(SMachineCoreCountChangePacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> ClientPacketHandler.handleMachineCoreCountChangePacket(packet.pos, packet.count));
    }
}
