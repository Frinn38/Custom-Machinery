package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SUpdateMachinesPacket {

    private Map<ResourceLocation, CustomMachine> machines;

    public SUpdateMachinesPacket(Map<ResourceLocation, CustomMachine> machines) {
        this.machines = machines;
    }

    public static void encode(SUpdateMachinesPacket pkt, PacketBuffer buf) {
        buf.writeInt(pkt.machines.size());
        pkt.machines.forEach((id, machine) -> {
            try {
                buf.writeResourceLocation(id);
                buf.func_240629_a_(CustomMachine.CODEC, machine);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static SUpdateMachinesPacket decode(PacketBuffer buf) {
        Map<ResourceLocation, CustomMachine> map = new HashMap<>();
        int size = buf.readInt();
        for(int i = 0; i < size; i++) {
            try {
                ResourceLocation id = buf.readResourceLocation();
                CustomMachine machine = buf.func_240628_a_(CustomMachine.CODEC);
                machine.setId(id);
                map.put(id, machine);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new SUpdateMachinesPacket(map);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> {
                CustomMachinery.MACHINES.clear();
                CustomMachinery.MACHINES.putAll(machines);
                Registration.GROUP.fill(NonNullList.create());
            });
        }
        context.get().setPacketHandled(true);
    }
}
