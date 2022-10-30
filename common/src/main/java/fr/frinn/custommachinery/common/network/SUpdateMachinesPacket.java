package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.NonNullList;
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
        this.machines.forEach((id, machine) -> {
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

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnvironment() == Env.CLIENT) {
            context.queue(() -> {
                CustomMachinery.MACHINES.clear();
                CustomMachinery.MACHINES.putAll(machines);
                Registration.GROUP.fillItemList(NonNullList.create());
            });
        }
    }
}
