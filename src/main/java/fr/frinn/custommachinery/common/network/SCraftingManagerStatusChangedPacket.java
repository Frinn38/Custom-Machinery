package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SCraftingManagerStatusChangedPacket {

    private final BlockPos pos;
    private final MachineStatus status;

    public SCraftingManagerStatusChangedPacket(BlockPos pos, MachineStatus status) {
        this.pos = pos;
        this.status = status;
    }

    public static void encode(SCraftingManagerStatusChangedPacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeEnumValue(pkt.status);
    }

    public static SCraftingManagerStatusChangedPacket decode(PacketBuffer buf) {
        BlockPos pos = buf.readBlockPos();
        MachineStatus status = buf.readEnumValue(MachineStatus.class);
        return new SCraftingManagerStatusChangedPacket(pos, status);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
            context.get().enqueueWork(() -> ClientPacketHandler.handleCraftingManagerStatusChangedPacket(this.pos, this.status));
        context.get().setPacketHandled(true);
    }
}
