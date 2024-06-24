package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.util.FileUtils;
import fr.frinn.custommachinery.common.util.MachineList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CEditMachinePacket(CustomMachine machine) implements CustomPacketPayload {

    public static final Type<CEditMachinePacket> TYPE = new Type<>(CustomMachinery.rl("edit_machine"));

    public static final StreamCodec<FriendlyByteBuf, CEditMachinePacket> CODEC = new StreamCodec<>() {
        @Override
        public CEditMachinePacket decode(FriendlyByteBuf buf) {
            MachineLocation location = MachineLocation.CODEC.fromNetwork(buf);
            CustomMachine machine = CustomMachine.CODEC.fromNetwork(buf);
            machine.setLocation(location);
            return new CEditMachinePacket(machine);
        }

        @Override
        public void encode(FriendlyByteBuf buf, CEditMachinePacket packet) {
            MachineLocation.CODEC.toNetwork(packet.machine.getLocation(), buf);
            CustomMachine.CODEC.toNetwork(packet.machine, buf);
        }
    };

    @Override
    public Type<CEditMachinePacket> type() {
        return TYPE;
    }

    public static void handle(CEditMachinePacket packet, IPayloadContext context) {
        if(context.player() instanceof ServerPlayer player && player.hasPermissions(2)) {
            context.enqueueWork(() -> {
                FileUtils.writeMachineJson(player.server, packet.machine);
                CustomMachinery.MACHINES.replace(packet.machine.getId(), packet.machine);
                MachineList.refreshAllMachines();
                PacketDistributor.sendToAllPlayers(new SUpdateMachinesPacket(CustomMachinery.MACHINES));
            });
        }
    }
}
