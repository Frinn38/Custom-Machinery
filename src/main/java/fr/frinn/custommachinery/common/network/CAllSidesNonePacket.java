package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record CAllSidesNonePacket(int containerId, String componentId) implements CustomPacketPayload {

    public static final Type<CAllSidesNonePacket> TYPE = new Type<>(CustomMachinery.rl("all_sides_none"));

    public static final StreamCodec<ByteBuf, CAllSidesNonePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            CAllSidesNonePacket::containerId,
            ByteBufCodecs.STRING_UTF8,
            CAllSidesNonePacket::componentId,
            CAllSidesNonePacket::new
    );

    @Override
    public Type<CAllSidesNonePacket> type() {
        return TYPE;
    }

    public static void handle(CAllSidesNonePacket packet, IPayloadContext context) {
        if(context.player() instanceof ServerPlayer player) {
            context.enqueueWork(() -> {
                if(player.containerMenu.containerId == packet.containerId && player.containerMenu instanceof CustomMachineContainer container) {
                    Optional<ISideConfigComponent> component = container.getTile().getComponentManager().getConfigComponentById(packet.componentId());
                    if(component.isPresent()) {
                        SideConfig config = component.get().getConfig();
                        for(RelativeSide side : RelativeSide.values())
                            config.setSideMode(side, SideMode.NONE);
                    }
                }
            });
        }
    }
}
