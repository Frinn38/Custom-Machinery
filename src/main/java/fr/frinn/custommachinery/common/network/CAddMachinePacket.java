package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.machine.MachineLocation.Loader;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.util.FileUtils;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CAddMachinePacket(String id, Component name, boolean kubejs) implements CustomPacketPayload {

    public static final Type<CAddMachinePacket> TYPE = new Type<>(CustomMachinery.rl("add_machine"));

    public static final StreamCodec<ByteBuf, CAddMachinePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CAddMachinePacket::id,
            ByteBufCodecs.fromCodec(TextComponentUtils.CODEC.codec()),
            CAddMachinePacket::name,
            ByteBufCodecs.BOOL,
            CAddMachinePacket::kubejs,
            CAddMachinePacket::new
    );

    @Override
    public Type<CAddMachinePacket> type() {
        return TYPE;
    }

    public static void handle(CAddMachinePacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.getServer() != null && Utils.canPlayerManageMachines(player)) {
            context.enqueueWork(() -> {
                CustomMachinery.LOGGER.info("Player: {} added new Machine: {}", player.getName().getString(), packet.id);
                CustomMachine newMachine = new CustomMachineBuilder().setLocation(MachineLocation.fromLoader(packet.kubejs ? Loader.KUBEJS : Loader.DEFAULT, ResourceLocation.fromNamespaceAndPath(CustomMachinery.MODID, packet.id), "")).setName(packet.name).build();
                FileUtils.writeNewMachineJson(player.getServer(), newMachine, packet.kubejs);
            });
        }
    }
}
