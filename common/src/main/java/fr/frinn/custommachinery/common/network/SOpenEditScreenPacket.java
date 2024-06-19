package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SOpenEditScreenPacket extends BaseS2CMessage {

    private final ResourceLocation machine;

    public SOpenEditScreenPacket(ResourceLocation machine) {
        this.machine = machine;
    }

    @Override
    public MessageType getType() {
        return PacketManager.OPEN_EDIT_SCREEN;
    }

    public static SOpenEditScreenPacket read(FriendlyByteBuf buf) {
        return new SOpenEditScreenPacket(buf.readResourceLocation());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.machine);
    }

    @Override
    public void handle(PacketContext context) {
        if(context.getEnvironment() == Env.CLIENT)
            ClientPacketHandler.handleOpenEditScreenPacket(this.machine);
    }
}
