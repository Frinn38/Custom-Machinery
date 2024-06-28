package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.util.FileUtils;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CRemoveMachinePacket(ResourceLocation id) implements CustomPacketPayload {

    public static final Type<CRemoveMachinePacket> TYPE = new Type<>(CustomMachinery.rl("remove_machine"));

    public static final StreamCodec<FriendlyByteBuf, CRemoveMachinePacket> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            CRemoveMachinePacket::id,
            CRemoveMachinePacket::new
    );

    @Override
    public Type<CRemoveMachinePacket> type() {
        return TYPE;
    }

    public static void handle(CRemoveMachinePacket packet, IPayloadContext context) {
        if(context.player() instanceof ServerPlayer player && player.getServer() != null && Utils.canPlayerManageMachines(player)) {
            CustomMachine machine = CustomMachinery.MACHINES.get(packet.id);
            if(machine != null)
                context.enqueueWork(() -> {
                    CustomMachinery.LOGGER.info("Player: {} removed machine: {}", player.getName().getString(), packet.id);
                    if(FileUtils.deleteMachineJson(player.getServer(), machine.getLocation())) {
                        CustomMachinery.MACHINES.remove(packet.id);
                        PacketDistributor.sendToAllPlayers(new SUpdateMachinesPacket(CustomMachinery.MACHINES));
                    }
                });
        }
    }
}
