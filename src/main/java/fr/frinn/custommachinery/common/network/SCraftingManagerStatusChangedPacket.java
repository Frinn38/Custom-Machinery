package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SCraftingManagerStatusChangedPacket {

    private final BlockPos pos;
    private final MachineStatus status;

    public SCraftingManagerStatusChangedPacket(BlockPos pos, MachineStatus status) {
        this.pos = pos;
        this.status = status;
    }

    public static void encode(SCraftingManagerStatusChangedPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeEnum(pkt.status);
    }

    public static SCraftingManagerStatusChangedPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        MachineStatus status = buf.readEnum(MachineStatus.class);
        return new SCraftingManagerStatusChangedPacket(pos, status);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
            context.get().enqueueWork(() -> ClientPacketHandler.handleCraftingManagerStatusChangedPacket(this.pos, this.status));
        context.get().setPacketHandled(true);
    }
}
