package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

/**
 * 0:TOP, 1:BOTTOM, 2:FRONT, 3:RIGHT, 4:BACK, 5:LEFT, 6:INPUT, 7:OUTPUT
 */
public record CChangeSideModePacket(int containerId, String id, byte side, boolean next) implements CustomPacketPayload {

    public static final Type<CChangeSideModePacket> TYPE = new Type<>(CustomMachinery.rl("change_side_mode"));

    public static final StreamCodec<ByteBuf, CChangeSideModePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            CChangeSideModePacket::containerId,
            ByteBufCodecs.STRING_UTF8,
            CChangeSideModePacket::id,
            ByteBufCodecs.BYTE,
            CChangeSideModePacket::side,
            ByteBufCodecs.BOOL,
            CChangeSideModePacket::next,
            CChangeSideModePacket::new
    );

    @Override
    public Type<CChangeSideModePacket> type() {
        return TYPE;
    }

    public static void handle(CChangeSideModePacket packet, IPayloadContext context) {
        if(context.player() instanceof ServerPlayer player) {
            context.enqueueWork(() -> {
                if(player.containerMenu.containerId == packet.containerId && player.containerMenu instanceof CustomMachineContainer container) {
                    Optional<ISideConfigComponent> component = container.getTile().getComponentManager().getConfigComponentById(packet.id);
                    if(component.isPresent()) {
                        SideConfig config = component.get().getConfig();
                        switch (packet.side) {
                            case 6 -> config.setAutoInput(!config.isAutoInput());
                            case 7 -> config.setAutoOutput(!config.isAutoOutput());
                            default -> {
                                RelativeSide side = RelativeSide.values()[packet.side];
                                if(packet.next)
                                    config.setSideMode(side, config.getSideMode(side).next());
                                else
                                    config.setSideMode(side, config.getSideMode(side).previous());
                            }
                        }
                    }
                }
            });
        }
    }
}
