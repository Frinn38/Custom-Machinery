package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CGuiElementClickPacket(int element, byte click) implements CustomPacketPayload {

    public static final Type<CGuiElementClickPacket> TYPE = new Type<>(CustomMachinery.rl("element_clicked"));

    public static final StreamCodec<ByteBuf, CGuiElementClickPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            CGuiElementClickPacket::element,
            ByteBufCodecs.BYTE,
            CGuiElementClickPacket::click,
            CGuiElementClickPacket::new
    );

    @Override
    public Type<CGuiElementClickPacket> type() {
        return TYPE;
    }

    public static void handle(CGuiElementClickPacket packet, IPayloadContext context) {
        if(context.player() instanceof ServerPlayer player)
            context.enqueueWork(() -> {
                if(player.containerMenu instanceof CustomMachineContainer)
                    ((CustomMachineContainer)player.containerMenu).elementClicked(packet.element, packet.click);
            });
    }
}
