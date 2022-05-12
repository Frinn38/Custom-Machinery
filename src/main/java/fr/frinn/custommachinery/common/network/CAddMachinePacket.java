package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineLocation;
import fr.frinn.custommachinery.common.util.FileUtils;
import fr.frinn.custommachinery.common.util.Utils;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

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

    public static void encode(CAddMachinePacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.id);
        try {
            buf.writeWithCodec(MachineLocation.CODEC, pkt.machine.getLocation());
            buf.writeWithCodec(CustomMachine.CODEC, pkt.machine);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        buf.writeBoolean(pkt.shouldReload);
        buf.writeBoolean(pkt.writeToFile);
    }

    public static CAddMachinePacket decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        CustomMachine machine = CustomMachine.DUMMY;
        try {
            MachineLocation location = buf.readWithCodec(MachineLocation.CODEC);
            machine = buf.readWithCodec(CustomMachine.CODEC).setLocation(location);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        boolean shouldReload = buf.readBoolean();
        boolean writeToFile = buf.readBoolean();
        return new CAddMachinePacket(id, machine, shouldReload, writeToFile);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayer player = context.get().getSender();
            if(player != null && player.level.getServer() != null && Utils.canPlayerManageMachines(player) && this.machine != CustomMachine.DUMMY)
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
