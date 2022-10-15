package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class CChangeSideModePacket extends BaseC2SMessage {

    private final int containerID;
    private final String id;
    /**
     * 0:TOP, 1:BOTTOM, 2:FRONT, 3:RIGHT, 4:BACK, 5:LEFT, 6:INPUT, 7:OUTPUT
     */
    private final byte side;
    private final boolean next;

    public CChangeSideModePacket(int containerID, String id, byte side, boolean next) {
        this.containerID = containerID;
        this.id = id;
        this.side = side;
        this.next = next;
    }

    @Override
    public MessageType getType() {
        return PacketManager.CHANGE_SIDE_MODE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.containerID);
        buf.writeUtf(this.id);
        buf.writeByte(this.side);
        buf.writeBoolean(this.next);
    }

    public static CChangeSideModePacket decode(FriendlyByteBuf buf) {
        return new CChangeSideModePacket(buf.readVarInt(), buf.readUtf(), buf.readByte(), buf.readBoolean());
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnv() == EnvType.SERVER) {
            context.queue(() -> {
                Player player = context.getPlayer();
                if(player != null && player.containerMenu.containerId == this.containerID && player.containerMenu instanceof CustomMachineContainer container) {
                    Optional<ISideConfigComponent> component = container.getTile().componentManager.getConfigComponentById(this.id);
                    if(component.isPresent()) {
                        SideConfig config = component.get().getConfig();
                        switch (this.side) {
                            case 6 -> config.setAutoInput(!config.isAutoInput());
                            case 7 -> config.setAutoOutput(!config.isAutoOutput());
                            default -> {
                                RelativeSide side = RelativeSide.values()[this.side];
                                if(this.next)
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
