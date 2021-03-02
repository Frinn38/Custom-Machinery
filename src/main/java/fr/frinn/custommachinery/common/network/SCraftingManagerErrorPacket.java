package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SCraftingManagerErrorPacket {

    private BlockPos machinePos;
    private ITextComponent message;
    private boolean errored;

    public SCraftingManagerErrorPacket(BlockPos machinePos, ITextComponent message, boolean errored) {
        this.machinePos = machinePos;
        this.message = message;
        this.errored = errored;
    }

    public static void encode(SCraftingManagerErrorPacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.machinePos);
        buf.writeTextComponent(pkt.message);
        buf.writeBoolean(pkt.errored);
    }

    public static SCraftingManagerErrorPacket decode(PacketBuffer buf) {
        return new SCraftingManagerErrorPacket(buf.readBlockPos(), buf.readTextComponent(), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> {
                if(Minecraft.getInstance().world != null && Minecraft.getInstance().world.isAreaLoaded(this.machinePos, 1)) {
                    TileEntity tile = Minecraft.getInstance().world.getTileEntity(this.machinePos);
                    if(tile instanceof CustomMachineTile) {
                        if(this.errored)
                            ((CustomMachineTile)tile).craftingManager.setErrored(this.message);
                        else
                            ((CustomMachineTile)tile).craftingManager.setRunning();
                    }
                }
            });
        }
        context.get().setPacketHandled(true);
    }
}
