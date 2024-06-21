package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.guielement.ButtonGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.TaskDelayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CButtonGuiElementPacket(String id, boolean toggle) implements CustomPacketPayload {

    public static final Type<CButtonGuiElementPacket> TYPE = new Type<>(CustomMachinery.rl("button_gui_element"));

    public static final StreamCodec<ByteBuf, CButtonGuiElementPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CButtonGuiElementPacket::id,
            ByteBufCodecs.BOOL,
            CButtonGuiElementPacket::toggle,
            CButtonGuiElementPacket::new
    );

    @Override
    public Type<CButtonGuiElementPacket> type() {
        return TYPE;
    }

    public static void handle(CButtonGuiElementPacket packet, IPayloadContext context) {
        if(context.player() instanceof ServerPlayer player && player.getServer() != null) {
            if(player.containerMenu instanceof CustomMachineContainer container) {
                int holdTime = container.getTile().getGuiElements().stream()
                        .filter(element -> element instanceof ButtonGuiElement button && button.getId().equals(packet.id))
                        .findFirst()
                        .map(element -> ((ButtonGuiElement)element).getHoldTime())
                        .orElse(-1);
                if(holdTime == -1)
                    return;
                container.getTile().getComponentManager()
                        .getComponent(Registration.DATA_MACHINE_COMPONENT.get())
                        .ifPresent(component -> {
                            if(packet.toggle)
                                component.getData().putBoolean(packet.id, !component.getData().getBoolean(packet.id));
                            else {
                                component.getData().putBoolean(packet.id, true);
                                component.getManager().markDirty();
                                TaskDelayer.enqueue(holdTime, () -> {
                                    component.getData().putBoolean(packet.id, false);
                                    component.getManager().markDirty();
                                });
                            }
                        });
            }
        }
    }
}
