package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.util.FileUtils;
import fr.frinn.custommachinery.common.util.Utils;
import io.netty.handler.codec.EncoderException;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CAddMachinePacket extends BaseC2SMessage {

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

    @Override
    public MessageType getType() {
        return PacketManager.ADD_MACHINE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        try {
            buf.writeWithCodec(MachineLocation.CODEC, this.machine.getLocation());
            buf.writeWithCodec(CustomMachine.CODEC, this.machine);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        buf.writeBoolean(this.shouldReload);
        buf.writeBoolean(this.writeToFile);
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

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnv() == EnvType.SERVER) {
            Player player = context.getPlayer();
            if(player != null && player.getServer() != null && Utils.canPlayerManageMachines(player) && this.machine != CustomMachine.DUMMY)
                context.queue(() -> {
                    CustomMachinery.LOGGER.info("Player: " + player.getDisplayName().getString() + " added new Machine: " + id);
                    CustomMachinery.MACHINES.put(this.id, this.machine);
                    if(this.shouldReload)
                        new SUpdateMachinesPacket(CustomMachinery.MACHINES).sendToAll(player.getServer());
                    if(this.writeToFile && this.machine.getLocation().getLoader() == MachineLocation.Loader.DATAPACK)
                        FileUtils.writeMachineJSON(player.getServer(), this.machine);
                });
        }
    }
}
