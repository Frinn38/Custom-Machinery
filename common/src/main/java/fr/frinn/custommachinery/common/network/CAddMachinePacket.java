package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.machine.MachineLocation.Loader;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.util.FileUtils;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CAddMachinePacket extends BaseC2SMessage {

    private final String id;
    private final Component name;
    private final boolean kubejs;

    public CAddMachinePacket(String id, Component name, boolean kubejs) {
        this.id = id;
        this.name = name;
        this.kubejs = kubejs;
    }

    @Override
    public MessageType getType() {
        return PacketManager.ADD_MACHINE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.id);
        buf.writeComponent(this.name);
        buf.writeBoolean(this.kubejs);
    }

    public static CAddMachinePacket read(FriendlyByteBuf buf) {
        return new CAddMachinePacket(buf.readUtf(), buf.readComponent(), buf.readBoolean());
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() == Env.SERVER) {
            Player player = context.getPlayer();
            if(player != null && player.getServer() != null && Utils.canPlayerManageMachines(player))
                context.queue(() -> {
                    CustomMachinery.LOGGER.info("Player: " + player.getDisplayName().getString() + " added new Machine: " + this.id);
                    CustomMachine newMachine = new CustomMachineBuilder().setLocation(MachineLocation.fromLoader(this.kubejs ? Loader.KUBEJS : Loader.DEFAULT, new ResourceLocation(CustomMachinery.MODID, this.id), "")).setName(this.name).build();
                    FileUtils.writeNewMachineJson(player.getServer(), newMachine, this.kubejs);
                });
        }
    }
}
