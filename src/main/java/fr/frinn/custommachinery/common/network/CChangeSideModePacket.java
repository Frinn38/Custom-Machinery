package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.apiimpl.component.config.RelativeSide;
import fr.frinn.custommachinery.apiimpl.component.config.SideConfig;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class CChangeSideModePacket {

    private final int containerID;
    private final String id;
    private final byte side;
    private final boolean next;

    public CChangeSideModePacket(int containerID, String id, byte side, boolean next) {
        this.containerID = containerID;
        this.id = id;
        this.side = side;
        this.next = next;
    }

    public static void encode(CChangeSideModePacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.containerID);
        buf.writeUtf(pkt.id);
        buf.writeByte(pkt.side);
        buf.writeBoolean(pkt.next);
    }

    public static CChangeSideModePacket decode(FriendlyByteBuf buf) {
        return new CChangeSideModePacket(buf.readVarInt(), buf.readUtf(), buf.readByte(), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                if(player != null && player.containerMenu.containerId == this.containerID && player.containerMenu instanceof CustomMachineContainer container) {
                    Optional<ISideConfigComponent> component = container.tile.componentManager.getConfigComponentById(this.id);
                    if(component.isPresent()) {
                        SideConfig config = component.get().getConfig();
                        RelativeSide side = RelativeSide.values()[this.side];
                        if(this.next)
                            config.setSideMode(side, config.getSideMode(side).next());
                        else
                            config.setSideMode(side, config.getSideMode(side).previous());
                    }
                }
            });
        }
        context.get().setPacketHandled(true);
    }
}
