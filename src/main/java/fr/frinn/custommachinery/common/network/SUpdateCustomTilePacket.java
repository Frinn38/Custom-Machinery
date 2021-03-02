package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SUpdateCustomTilePacket {

    private BlockPos pos;
    private CompoundNBT nbt;

    public SUpdateCustomTilePacket(BlockPos pos, CompoundNBT nbt) {
        this.pos = pos;
        this.nbt = nbt;
    }

    public static void encode(SUpdateCustomTilePacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeCompoundTag(pkt.nbt);
    }

    public static SUpdateCustomTilePacket decode(PacketBuffer buf) {
        BlockPos pos = buf.readBlockPos();
        CompoundNBT nbt = buf.readCompoundTag();
        return new SUpdateCustomTilePacket(pos, nbt);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> {
                if(Minecraft.getInstance().world != null) {
                    TileEntity tile = Minecraft.getInstance().world.getTileEntity(this.pos);
                    if(tile instanceof CustomMachineTile) {
                        tile.handleUpdateTag(Minecraft.getInstance().world.getBlockState(this.pos), this.nbt);
                    }
                }
            });
        }
        context.get().setPacketHandled(true);
    }
}
