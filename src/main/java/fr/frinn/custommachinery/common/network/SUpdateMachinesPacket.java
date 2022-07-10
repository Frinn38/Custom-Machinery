package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SUpdateMachinesPacket {

    private final Map<ResourceLocation, CustomMachine> machines;

    public SUpdateMachinesPacket(Map<ResourceLocation, CustomMachine> machines) {
        this.machines = new HashMap<>(machines);
    }

    public static void encode(SUpdateMachinesPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.machines.size());
        pkt.machines.forEach((id, machine) -> {
            try {
                buf.writeResourceLocation(id);
                buf.writeWithCodec(MachineLocation.CODEC, machine.getLocation());
                buf.writeWithCodec(CustomMachine.CODEC, machine);
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        });
    }

    public static SUpdateMachinesPacket decode(FriendlyByteBuf buf) {
        Map<ResourceLocation, CustomMachine> map = new HashMap<>();
        int size = buf.readInt();
        for(int i = 0; i < size; i++) {
            try {
                ResourceLocation id = buf.readResourceLocation();
                MachineLocation location = buf.readWithCodec(MachineLocation.CODEC);
                CustomMachine machine = buf.readWithCodec(CustomMachine.CODEC);
                machine.setLocation(location);
                map.put(id, machine);
            } catch (EncoderException e) {
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
                Registration.GROUP.fillItemList(NonNullList.create());
            });
        }
        context.get().setPacketHandled(true);
    }
}
