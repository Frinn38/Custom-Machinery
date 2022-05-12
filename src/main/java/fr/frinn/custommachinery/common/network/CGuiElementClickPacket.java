package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CGuiElementClickPacket {

    private final int element;
    private final byte type;

    public CGuiElementClickPacket(int element, byte type) {
        this.element = element;
        this.type = type;
    }

    public static void encode(CGuiElementClickPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.element);
        buf.writeByte(pkt.type);
    }

    public static CGuiElementClickPacket decode(FriendlyByteBuf buf) {
        return new CGuiElementClickPacket(buf.readVarInt(), buf.readByte());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER)
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                if(player != null && player.containerMenu instanceof CustomMachineContainer)
                    ((CustomMachineContainer)player.containerMenu).elementClicked(this.element, this.type);
            });
        context.get().setPacketHandled(true);
    }
}
