package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SUpdateUpgradesPacket(List<MachineUpgrade> upgrades) implements CustomPacketPayload {

    public static final Type<SUpdateUpgradesPacket> TYPE = new Type<>(CustomMachinery.rl("update_upgrades"));

    public static final StreamCodec<FriendlyByteBuf, SUpdateUpgradesPacket> CODEC = StreamCodec.ofMember(SUpdateUpgradesPacket::write, SUpdateUpgradesPacket::read);

    @Override
    public Type<SUpdateUpgradesPacket> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.upgrades.size());
        this.upgrades.forEach(upgrade -> {
            try {
                MachineUpgrade.CODEC.toNetwork(upgrade, buf);
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        });
    }

    public static SUpdateUpgradesPacket read(FriendlyByteBuf buf) {
        List<MachineUpgrade> upgrades = new ArrayList<>();
        int size = buf.readVarInt();
        for(int i = 0; i < size; i++) {
            try {
                MachineUpgrade upgrade = MachineUpgrade.CODEC.fromNetwork(buf);
                upgrades.add(upgrade);
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        }
        return new SUpdateUpgradesPacket(upgrades);
    }

    public static void handle(SUpdateUpgradesPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> {
                CustomMachinery.UPGRADES.refresh(packet.upgrades);
            });
    }
}
