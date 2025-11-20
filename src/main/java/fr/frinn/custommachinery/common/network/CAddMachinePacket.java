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

public record CAddMachinePacket(String id, Component name, boolean kubejs, ResourceLocation template) implements CustomPacketPayload {

    public static final Type<CAddMachinePacket> TYPE = new Type<>(CustomMachinery.rl("add_machine"));
    public static final ResourceLocation EMPTY_TEMPLATE = CustomMachinery.rl("template/empty");

    public static final StreamCodec<ByteBuf, CAddMachinePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CAddMachinePacket::id,
            ByteBufCodecs.fromCodec(TextComponentUtils.CODEC.codec()),
            CAddMachinePacket::name,
            ByteBufCodecs.BOOL,
            CAddMachinePacket::kubejs,
            ResourceLocation.STREAM_CODEC,
            CAddMachinePacket::template,
            CAddMachinePacket::new
    );

    @Override
    public Type<CAddMachinePacket> type() {
        return TYPE;
    }

    public static void handle(CAddMachinePacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.getServer() != null && Utils.canPlayerManageMachines(player)) {
            context.enqueueWork(() -> {
                ResourceLocation loc = packet.id.contains(":") ? ResourceLocation.parse(packet.id) : CustomMachinery.rl(packet.id);
                CustomMachine newMachine;
                if(packet.template == EMPTY_TEMPLATE || !CustomMachinery.TEMPLATES.containsKey(packet.template)) {
                    CustomMachinery.LOGGER.info("Player: {} added new machine: {}", player.getName().getString(), loc);
                    newMachine = new CustomMachineBuilder().setLocation(MachineLocation.fromLoader(packet.kubejs ? Loader.KUBEJS : Loader.DEFAULT, loc, "", null, null)).setName(packet.name).build();
                } else {
                    CustomMachinery.LOGGER.info("Player: {} added new machine: {} from template: {}", player.getName().getString(), loc, packet.template.toString());
                    newMachine = new CustomMachineBuilder(CustomMachinery.TEMPLATES.get(packet.template).getFirst()).setLocation(MachineLocation.fromLoader(packet.kubejs ? Loader.KUBEJS : Loader.DEFAULT, loc, "", null, null)).setName(packet.name).build();
                }
                FileUtils.writeNewMachineJson(player.getServer(), newMachine, packet.kubejs);
            });
        }
    }
}
