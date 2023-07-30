package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class CGuiElementClickPacket extends BaseC2SMessage {

    private final int element;
    private final byte type;

    public CGuiElementClickPacket(int element, byte type) {
        this.element = element;
        this.type = type;
    }

    @Override
    public MessageType getType() {
        return PacketManager.ELEMENT_CLICKED;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.element);
        buf.writeByte(this.type);
    }

    public static CGuiElementClickPacket read(FriendlyByteBuf buf) {
        return new CGuiElementClickPacket(buf.readVarInt(), buf.readByte());
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnvironment() == Env.SERVER)
            context.queue(() -> {
                Player player = context.getPlayer();
                if(player != null && player.containerMenu instanceof CustomMachineContainer)
                    ((CustomMachineContainer)player.containerMenu).elementClicked(this.element, this.type);
            });
    }
}
