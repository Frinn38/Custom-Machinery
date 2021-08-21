package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SRefreshCustomMachineTilePacket {

    private BlockPos pos;
    private ResourceLocation machine;

    public SRefreshCustomMachineTilePacket(BlockPos pos, ResourceLocation machine) {
        this.pos = pos;
        this.machine = machine;
    }

    public static void encode(SRefreshCustomMachineTilePacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeResourceLocation(pkt.machine);
    }

    public static SRefreshCustomMachineTilePacket decode(PacketBuffer buf) {
        BlockPos pos = buf.readBlockPos();
        ResourceLocation machine = buf.readResourceLocation();
        return new SRefreshCustomMachineTilePacket(pos, machine);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> {
                if(Minecraft.getInstance().world != null) {
                    TileEntity tile = Minecraft.getInstance().world.getTileEntity(this.pos);
                    if(tile instanceof CustomMachineTile) {
                        CustomMachineTile machineTile = (CustomMachineTile) tile;
                        machineTile.setId(this.machine);
                        machineTile.requestModelDataUpdate();
                        Minecraft.getInstance().world.notifyBlockUpdate(this.pos, machineTile.getBlockState(), machineTile.getBlockState(), Constants.BlockFlags.RERENDER_MAIN_THREAD);
                    }
                }
            });
        }
        context.get().setPacketHandled(true);
    }
}
