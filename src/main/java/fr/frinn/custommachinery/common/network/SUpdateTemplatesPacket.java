package fr.frinn.custommachinery.common.network;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.machine.UpgradedCustomMachine;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SUpdateTemplatesPacket(Map<ResourceLocation, Pair<CustomMachine, Component>> templates) implements CustomPacketPayload {

    public static final Type<SUpdateTemplatesPacket> TYPE = new Type<>(CustomMachinery.rl("update_templates"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SUpdateTemplatesPacket> CODEC = new StreamCodec<>() {
        @Override
        public SUpdateTemplatesPacket decode(RegistryFriendlyByteBuf buf) {
            Map<ResourceLocation, Pair<CustomMachine, Component>> map = new HashMap<>();
            int size = buf.readInt();
            for(int i = 0; i < size; i++) {
                try {
                    MachineLocation location = MachineLocation.CODEC.fromNetwork(buf);
                    CustomMachine machine;
                    if(buf.readBoolean()) {
                        ResourceLocation parent = buf.readResourceLocation();
                        machine = UpgradedCustomMachine.makeCodec(map.get(parent).getFirst()).fromNetwork(buf);
                    } else {
                        machine = CustomMachine.CODEC.fromNetwork(buf);
                    }
                    machine.setLocation(location);
                    Component tooltip = TextComponentUtils.CODEC.fromNetwork(buf);
                    map.put(location.id(), Pair.of(machine, tooltip));
                } catch (EncoderException e) {
                    e.printStackTrace();
                }
            }
            return new SUpdateTemplatesPacket(map);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SUpdateTemplatesPacket packet) {
            buf.writeInt(packet.templates.size());
            packet.templates.values()
                    .stream()
                    .map(Pair::getFirst)
                    .sorted(Comparators.PARENT_MACHINE_FIRST)
                    .forEach(machine -> {
                        try {
                            MachineLocation.CODEC.toNetwork(machine.getLocation(), buf);
                            if(machine instanceof UpgradedCustomMachine upgradedMachine) {
                                buf.writeBoolean(true);
                                buf.writeResourceLocation(((UpgradedCustomMachine) machine).getParentId());
                                UpgradedCustomMachine.makeCodec(packet.templates.get(upgradedMachine.getParentId()).getFirst()).toNetwork(upgradedMachine, buf);
                                TextComponentUtils.CODEC.toNetwork(packet.templates.get(machine.getId()).getSecond(), buf);
                            } else {
                                buf.writeBoolean(false);
                                CustomMachine.CODEC.toNetwork(machine, buf);
                                TextComponentUtils.CODEC.toNetwork(packet.templates.get(machine.getId()).getSecond(), buf);
                            }
                        } catch (EncoderException e) {
                            e.printStackTrace();
                        }
                    });
        }
    };

    @Override
    public Type<SUpdateTemplatesPacket> type() {
        return TYPE;
    }

    public static void handle(SUpdateTemplatesPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> ClientPacketHandler.handleUpdateTemplatesPacket(packet.templates));
    }
}
