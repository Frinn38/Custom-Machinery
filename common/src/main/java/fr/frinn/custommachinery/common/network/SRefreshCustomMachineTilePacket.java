package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SRefreshCustomMachineTilePacket extends BaseS2CMessage {

    private final BlockPos pos;
    private final ResourceLocation machine;

    public SRefreshCustomMachineTilePacket(BlockPos pos, ResourceLocation machine) {
        this.pos = pos;
        this.machine = machine;
    }

    @Override
    public MessageType getType() {
        return PacketManager.REFRESH_MACHINE_TILE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeResourceLocation(this.machine);
    }

    public static SRefreshCustomMachineTilePacket read(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ResourceLocation machine = buf.readResourceLocation();
        return new SRefreshCustomMachineTilePacket(pos, machine);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() == Env.CLIENT)
            context.queue(() -> ClientPacketHandler.handleRefreshCustomMachineTilePacket(this.pos, this.machine));
    }
}
