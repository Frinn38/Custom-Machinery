package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.UpgradeLocation;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SUpdateUpgradesPacket(Map<UpgradeLocation, MachineUpgrade> upgrades) implements CustomPacketPayload {

    public static final Type<SUpdateUpgradesPacket> TYPE = new Type<>(CustomMachinery.rl("update_upgrades"));

    public static final StreamCodec<FriendlyByteBuf, SUpdateUpgradesPacket> CODEC = StreamCodec.ofMember(SUpdateUpgradesPacket::write, SUpdateUpgradesPacket::read);

    @Override
    public Type<SUpdateUpgradesPacket> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.upgrades.size());
        this.upgrades.forEach((location, upgrade) -> {
            try {
                UpgradeLocation.CODEC.toNetwork(location, buf);
                MachineUpgrade.CODEC.toNetwork(upgrade, buf);
            } catch (EncoderException e) {
                CustomMachinery.LOGGER.error("Error while sending CM upgrades to client", e);
            }
        });
    }

    public static SUpdateUpgradesPacket read(FriendlyByteBuf buf) {
        Map<UpgradeLocation, MachineUpgrade> upgrades = new HashMap<>();
        int size = buf.readVarInt();
        for(int i = 0; i < size; i++) {
            try {
                UpgradeLocation location = UpgradeLocation.CODEC.fromNetwork(buf);
                MachineUpgrade upgrade = MachineUpgrade.CODEC.fromNetwork(buf);
                upgrades.put(location, upgrade);
            } catch (EncoderException e) {
                CustomMachinery.LOGGER.error("Error while receiving CM upgrades from server", e);
            }
        }
        return new SUpdateUpgradesPacket(upgrades);
    }

    public static void handle(SUpdateUpgradesPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> ClientPacketHandler.handleUpdateUpgradesPacket(packet.upgrades));
    }
}
