package fr.frinn.custommachinery.common.network;

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
    private CraftingManager.STATUS status;

    public SCraftingManagerStatusChangedPacket(BlockPos pos, CraftingManager.STATUS status) {
        this.pos = pos;
        this.status = status;
    }

    public static void encode(SCraftingManagerStatusChangedPacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeEnumValue(pkt.status);
    }

    public static SCraftingManagerStatusChangedPacket decode(PacketBuffer buf) {
        BlockPos pos = buf.readBlockPos();
        CraftingManager.STATUS status = buf.readEnumValue(CraftingManager.STATUS.class);
        return new SCraftingManagerStatusChangedPacket(pos, status);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> {
                if(Minecraft.getInstance().world != null) {
                    TileEntity tile = Minecraft.getInstance().world.getTileEntity(this.pos);
                    if(tile instanceof CustomMachineTile) {
                        CraftingManager manager = ((CustomMachineTile) tile).craftingManager;
                        if(this.status != manager.getStatus())
                        switch (this.status) {
                            case IDLE:
                                manager.setIdle();
                                break;
                            case ERRORED:
                                manager.setErrored(StringTextComponent.EMPTY);
                                break;
                            case RUNNING:
                                manager.setRunning();
                                break;
                        }
                    }
                }
            });
        }
        context.get().setPacketHandled(true);
    }
}
