package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SUpdateCustomTileLightPacket {

    private BlockPos pos;

    public SUpdateCustomTileLightPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(SUpdateCustomTileLightPacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static SUpdateCustomTileLightPacket decode(PacketBuffer buf) {
        return new SUpdateCustomTileLightPacket(buf.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientWorld world = Minecraft.getInstance().world;
            if (world != null) {
                TileEntity tile = world.getTileEntity(this.pos);
                if(tile instanceof CustomMachineTile)
                    ((CustomMachineTile)tile).changeLightState();
            }
        });
        context.get().setPacketHandled(true);
    }
}
