package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class SUpdateMachineAppearancePacket extends BaseS2CMessage {

    private final BlockPos pos;
    @Nullable
    private final MachineAppearance appearance;

    public SUpdateMachineAppearancePacket(BlockPos pos, @Nullable MachineAppearance appearance) {
        this.pos = pos;
        this.appearance = appearance;
    }

    @Override
    public MessageType getType() {
        return PacketManager.UPDATE_MACHINE_APPEARANCE;
    }

    @Override
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

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() == Env.CLIENT)
            context.queue(() -> ClientPacketHandler.handleUpdateMachineAppearancePacket(this.pos, this.appearance));
    }
}
