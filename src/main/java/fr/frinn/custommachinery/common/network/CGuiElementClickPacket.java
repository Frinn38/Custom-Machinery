package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CGuiElementClickPacket {

    private int element;
    private byte type;

    public CGuiElementClickPacket(int element, byte type) {
        this.element = element;
        this.type = type;
    }

    public static void encode(CGuiElementClickPacket pkt, PacketBuffer buf) {
        buf.writeVarInt(pkt.element);
        buf.writeByte(pkt.type);
    }

    public static CGuiElementClickPacket decode(PacketBuffer buf) {
        return new CGuiElementClickPacket(buf.readVarInt(), buf.readByte());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER)
            context.get().enqueueWork(() -> {
                ServerPlayerEntity player = context.get().getSender();
                if(player != null && player.openContainer instanceof CustomMachineContainer)
                    ((CustomMachineContainer)player.openContainer).elementClicked(this.element, this.type);
            });
        context.get().setPacketHandled(true);
    }
}
