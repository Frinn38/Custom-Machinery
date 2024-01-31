package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.machine.UpgradedCustomMachine;
import fr.frinn.custommachinery.common.util.Comparators;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SUpdateMachinesPacket extends BaseS2CMessage {

    private final Map<ResourceLocation, CustomMachine> machines;

    public SUpdateMachinesPacket(Map<ResourceLocation, CustomMachine> machines) {
        this.machines = new HashMap<>(machines);
    }

    @Override
    public MessageType getType() {
        return PacketManager.UPDATE_MACHINES;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.machines.size());
        this.machines.values()
                .stream()
                .sorted(Comparators.PARENT_MACHINE_FIRST)
                .forEach(machine -> {
            try {
                MachineLocation.CODEC.toNetwork(machine.getLocation(), buf);
                if(machine instanceof UpgradedCustomMachine upgradedMachine) {
                    buf.writeBoolean(true);
                    buf.writeResourceLocation(((UpgradedCustomMachine) machine).getParentId());
                    UpgradedCustomMachine.makeCodec(this.machines.get(upgradedMachine.getParentId())).toNetwork(upgradedMachine, buf);
                } else {
                    buf.writeBoolean(false);
                    CustomMachine.CODEC.toNetwork(machine, buf);
                }
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        });
    }

    public static SUpdateMachinesPacket read(FriendlyByteBuf buf) {
        Map<ResourceLocation, CustomMachine> map = new HashMap<>();
        int size = buf.readInt();
        for(int i = 0; i < size; i++) {
            try {
                MachineLocation location = MachineLocation.CODEC.fromNetwork(buf);
                CustomMachine machine;
                if(buf.readBoolean()) {
                    ResourceLocation parent = buf.readResourceLocation();
                    machine = UpgradedCustomMachine.makeCodec(map.get(parent)).fromNetwork(buf);
                } else {
                    machine = CustomMachine.CODEC.fromNetwork(buf);
                }
                machine.setLocation(location);
                map.put(location.getId(), machine);
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        }
        return new SUpdateMachinesPacket(map);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnvironment() == Env.CLIENT)
            context.queue(() -> ClientPacketHandler.handleUpdateMachinesPacket(this.machines));
    }
}
