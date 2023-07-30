package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class SUpdateMachineStatusPacket extends BaseS2CMessage {

    private final BlockPos pos;
    private final MachineStatus status;

    public SUpdateMachineStatusPacket(BlockPos pos, MachineStatus status) {
        this.pos = pos;
        this.status = status;
    }

    @Override
    public MessageType getType() {
        return PacketManager.UPDATE_MACHINE_STATUS;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeEnum(this.status);
    }

    public static SUpdateMachineStatusPacket read(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        MachineStatus status = buf.readEnum(MachineStatus.class);
        return new SUpdateMachineStatusPacket(pos, status);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() == Env.CLIENT)
            context.queue(() -> ClientPacketHandler.handleMachineStatusChangedPacket(this.pos, this.status));
    }
}
