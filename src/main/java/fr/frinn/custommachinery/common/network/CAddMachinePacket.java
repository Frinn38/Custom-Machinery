package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineLocation;
import fr.frinn.custommachinery.common.util.FileUtils;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.io.IOException;
import java.util.function.Supplier;

public class CAddMachinePacket {

    private final ResourceLocation id;
    private final CustomMachine machine;
    private final boolean shouldReload;
    private final boolean writeToFile;

    public CAddMachinePacket(ResourceLocation id, CustomMachine machine, boolean shouldReload, boolean writeToFile) {
        this.id = id;
        this.machine = machine;
        this.shouldReload = shouldReload;
        this.writeToFile = writeToFile;
    }

    public static void encode(CAddMachinePacket pkt, PacketBuffer buf) {
        buf.writeResourceLocation(pkt.id);
        try {
            buf.func_240629_a_(MachineLocation.CODEC, pkt.machine.getLocation());
            buf.func_240629_a_(CustomMachine.CODEC, pkt.machine);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buf.writeBoolean(pkt.shouldReload);
        buf.writeBoolean(pkt.writeToFile);
    }

    public static CAddMachinePacket decode(PacketBuffer buf) {
        ResourceLocation id = buf.readResourceLocation();
        CustomMachine machine = CustomMachine.DUMMY;
        try {
            MachineLocation location = buf.func_240628_a_(MachineLocation.CODEC);
            machine = buf.func_240628_a_(CustomMachine.CODEC).setLocation(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean shouldReload = buf.readBoolean();
        boolean writeToFile = buf.readBoolean();
        return new CAddMachinePacket(id, machine, shouldReload, writeToFile);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayerEntity player = context.get().getSender();
            if(player != null && player.world.getServer() != null && Utils.canPlayerManageMachines(player) && this.machine != CustomMachine.DUMMY)
            context.get().enqueueWork(() -> {
                CustomMachinery.LOGGER.info("Player: " + player.getDisplayName().getString() + " added new Machine: " + id);
                CustomMachinery.MACHINES.put(this.id, this.machine);
                if(this.shouldReload)
                    NetworkManager.CHANNEL.send(PacketDistributor.ALL.noArg(), new SUpdateMachinesPacket(CustomMachinery.MACHINES));
                if(this.writeToFile && this.machine.getLocation().getLoader() == MachineLocation.Loader.DATAPACK)
                    FileUtils.writeMachineJSON(this.machine);
            });
        }
        context.get().setPacketHandled(true);
    }
}
