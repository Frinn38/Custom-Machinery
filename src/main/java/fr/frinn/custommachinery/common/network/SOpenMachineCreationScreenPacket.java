package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SOpenMachineCreationScreenPacket() implements CustomPacketPayload {

    public static final Type<SOpenMachineCreationScreenPacket> TYPE = new Type<>(CustomMachinery.rl("open_machine_creation_screen"));

    public static final StreamCodec<ByteBuf, SOpenMachineCreationScreenPacket> CODEC = StreamCodec.unit(new SOpenMachineCreationScreenPacket());

    @Override
    public Type<SOpenMachineCreationScreenPacket> type() {
        return TYPE;
    }

    public static void handle(SOpenMachineCreationScreenPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            ClientPacketHandler.handleOpenMachineCreationScreenPacket();
    }
}