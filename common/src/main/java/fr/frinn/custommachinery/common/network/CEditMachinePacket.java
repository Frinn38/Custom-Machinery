package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.util.FileUtils;
import fr.frinn.custommachinery.common.util.MachineList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class CEditMachinePacket extends BaseC2SMessage {

    private final CustomMachine machine;

    public CEditMachinePacket(CustomMachine machine) {
        this.machine = machine;
    }

    @Override
    public MessageType getType() {
        return PacketManager.EDIT_MACHINE;
    }

    public static CEditMachinePacket read(FriendlyByteBuf buf) {
        MachineLocation location = MachineLocation.CODEC.fromNetwork(buf);
        CustomMachine machine = CustomMachine.CODEC.fromNetwork(buf);
        machine.setLocation(location);
        return new CEditMachinePacket(machine);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        MachineLocation.CODEC.toNetwork(this.machine.getLocation(), buf);
        CustomMachine.CODEC.toNetwork(this.machine, buf);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnvironment() == Env.SERVER && context.getPlayer() instanceof ServerPlayer player && player.hasPermissions(2)) {
            context.queue(() -> {
                FileUtils.writeMachineJson(player.server, this.machine);
                CustomMachinery.MACHINES.replace(this.machine.getId(), this.machine);
                MachineList.refreshAllMachines();
            });
            new SUpdateMachinesPacket(CustomMachinery.MACHINES).sendToAll(player.server);
        }
    }
}
