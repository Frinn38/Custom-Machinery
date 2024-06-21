package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SUpdateMachineAppearancePacket(BlockPos pos, MachineAppearance appearance) implements CustomPacketPayload {

    public static final Type<SUpdateMachineAppearancePacket> TYPE = new Type<>(CustomMachinery.rl("update_machine_appearance"));

    public static final StreamCodec<FriendlyByteBuf, SUpdateMachineAppearancePacket> CODEC = StreamCodec.ofMember(SUpdateMachineAppearancePacket::write, SUpdateMachineAppearancePacket::read);

    @Override
    public Type<SUpdateMachineAppearancePacket> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        if(this.appearance == null)
            buf.writeBoolean(true);
        else {
            buf.writeBoolean(false);
            MachineAppearance.CODEC.toNetwork(this.appearance.getProperties(), buf);
        }
    }

    public static SUpdateMachineAppearancePacket read(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        if(buf.readBoolean())
            return new SUpdateMachineAppearancePacket(pos, null);
        else
            return new SUpdateMachineAppearancePacket(pos, new MachineAppearance(MachineAppearance.CODEC.fromNetwork(buf)));
    }

    public static void handle(SUpdateMachineAppearancePacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> ClientPacketHandler.handleUpdateMachineAppearancePacket(packet.pos, packet.appearance));
    }
}
