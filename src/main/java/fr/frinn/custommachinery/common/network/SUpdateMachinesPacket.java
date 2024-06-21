package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.machine.UpgradedCustomMachine;
import fr.frinn.custommachinery.common.util.Comparators;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SUpdateMachinesPacket(Map<ResourceLocation, CustomMachine> machines) implements CustomPacketPayload {

    public static final Type<SUpdateMachinesPacket> TYPE = new Type<>(CustomMachinery.rl("update_machines"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SUpdateMachinesPacket> CODEC = new StreamCodec<>() {
        @Override
        public SUpdateMachinesPacket decode(RegistryFriendlyByteBuf buf) {
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
        public void encode(RegistryFriendlyByteBuf buf, SUpdateMachinesPacket packet) {
            buf.writeInt(packet.machines.size());
            packet.machines.values()
                    .stream()
                    .sorted(Comparators.PARENT_MACHINE_FIRST)
                    .forEach(machine -> {
                        try {
                            MachineLocation.CODEC.toNetwork(machine.getLocation(), buf);
                            if(machine instanceof UpgradedCustomMachine upgradedMachine) {
                                buf.writeBoolean(true);
                                buf.writeResourceLocation(((UpgradedCustomMachine) machine).getParentId());
                                UpgradedCustomMachine.makeCodec(packet.machines.get(upgradedMachine.getParentId())).toNetwork(upgradedMachine, buf);
                            } else {
                                buf.writeBoolean(false);
                                CustomMachine.CODEC.toNetwork(machine, buf);
                            }
                        } catch (EncoderException e) {
                            e.printStackTrace();
                        }
                    });
        }
    };

    @Override
    public Type<SUpdateMachinesPacket> type() {
        return TYPE;
    }

    public static void handle(SUpdateMachinesPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> ClientPacketHandler.handleUpdateMachinesPacket(packet.machines));
    }
}
