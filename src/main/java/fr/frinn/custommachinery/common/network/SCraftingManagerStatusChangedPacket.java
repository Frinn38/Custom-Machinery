package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SCraftingManagerStatusChangedPacket {

    private BlockPos pos;
    private MachineStatus status;

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
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> {
                if(Minecraft.getInstance().world != null) {
                    TileEntity tile = Minecraft.getInstance().world.getTileEntity(this.pos);
                    if(tile instanceof CustomMachineTile) {
                        CustomMachineTile machineTile = (CustomMachineTile)tile;
                        CraftingManager manager = machineTile.craftingManager;
                        if(this.status != manager.getStatus()) {
                            manager.setStatus(this.status);
                            machineTile.requestModelDataUpdate();
                        }
                    }
                }
            });
        }
        context.get().setPacketHandled(true);
    }
}
