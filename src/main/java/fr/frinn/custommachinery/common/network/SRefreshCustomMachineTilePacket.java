package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SRefreshCustomMachineTilePacket {

    private final BlockPos pos;
    private final ResourceLocation machine;

    public SRefreshCustomMachineTilePacket(BlockPos pos, ResourceLocation machine) {
        this.pos = pos;
        this.machine = machine;
    }

    public static void encode(SRefreshCustomMachineTilePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeResourceLocation(pkt.machine);
    }

    public static SRefreshCustomMachineTilePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ResourceLocation machine = buf.readResourceLocation();
        return new SRefreshCustomMachineTilePacket(pos, machine);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
            context.get().enqueueWork(() -> ClientPacketHandler.handleRefreshCustomMachineTilePacket(this.pos, this.machine));
        context.get().setPacketHandled(true);
    }
}
