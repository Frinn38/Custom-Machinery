package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SOpenUpgradeCreationScreenPacket() implements CustomPacketPayload {

    public static final Type<SOpenUpgradeCreationScreenPacket> TYPE = new Type<>(CustomMachinery.rl("open_upgrade_creation_screen"));

    public static final StreamCodec<ByteBuf, SOpenUpgradeCreationScreenPacket> CODEC = StreamCodec.unit(new SOpenUpgradeCreationScreenPacket());

    @Override
    public Type<SOpenUpgradeCreationScreenPacket> type() {
        return TYPE;
    }

    public static void handle(SOpenUpgradeCreationScreenPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            ClientPacketHandler.handleOpenUpgradeCreationScreenPacket();
    }
}
