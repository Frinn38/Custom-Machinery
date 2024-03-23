package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.util.FileUtils;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class CRemoveMachinePacket extends BaseC2SMessage {

    private final ResourceLocation id;

    public CRemoveMachinePacket(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public MessageType getType() {
        return PacketManager.REMOVE_MACHINE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
    }

    public static CRemoveMachinePacket read(FriendlyByteBuf buf) {
        return new CRemoveMachinePacket(buf.readResourceLocation());
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() == Env.SERVER) {
            CustomMachine machine = CustomMachinery.MACHINES.get(this.id);
            if(context.getPlayer() instanceof ServerPlayer player && player.getServer() != null && Utils.canPlayerManageMachines(player) && machine != null)
                context.queue(() -> {
                    CustomMachinery.LOGGER.info("Player: {} removed machine: {}", player.getDisplayName().getString(), this.id);
                    CustomMachinery.MACHINES.remove(this.id);
                    FileUtils.deleteMachineJson(player.getServer(), machine.getLocation());
                });
        }
    }
}
