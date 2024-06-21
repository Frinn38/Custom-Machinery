package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SUpdateMachineStatusPacket(BlockPos pos, MachineStatus status) implements CustomPacketPayload {

    public static final Type<SUpdateMachineStatusPacket> TYPE = new Type<>(CustomMachinery.rl("update_machine_status"));

    public static final StreamCodec<FriendlyByteBuf, SUpdateMachineStatusPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SUpdateMachineStatusPacket::pos,
            NeoForgeStreamCodecs.enumCodec(MachineStatus.class),
            SUpdateMachineStatusPacket::status,
            SUpdateMachineStatusPacket::new
    );

    @Override
    public Type<SUpdateMachineStatusPacket> type() {
        return TYPE;
    }

    public static void handle(SUpdateMachineStatusPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> ClientPacketHandler.handleMachineStatusChangedPacket(packet.pos, packet.status));
    }
}
