package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.upgrade.UpgradeLocation;
import fr.frinn.custommachinery.common.util.FileUtils;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CRemoveUpgradePacket(Identifier id) implements CustomPacketPayload {

    public static final Type<CRemoveUpgradePacket> TYPE = new Type<>(CustomMachinery.rl("remove_upgrade"));

    public static final StreamCodec<FriendlyByteBuf, CRemoveUpgradePacket> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            CRemoveUpgradePacket::id,
            CRemoveUpgradePacket::new
    );

    @Override
    public Type<CRemoveUpgradePacket> type() {
        return TYPE;
    }

    public static void handle(CRemoveUpgradePacket packet, IPayloadContext context) {
        if(context.player() instanceof ServerPlayer player && Utils.canPlayerManageMachines(player)) {
            CustomMachinery.UPGRADES.getAllUpgrades().keySet().stream().filter(loc -> loc.id().equals(packet.id)).findFirst().ifPresent(location -> context.enqueueWork(() -> {
                CustomMachinery.LOGGER.info("Player: {} removed upgrade: {}", player.getName().getString(), packet.id);
                if (FileUtils.deleteUpgradeJson(player.level().getServer(), location)) {
                    CustomMachinery.UPGRADES.removeUpgrade(packet.id);
                    PacketDistributor.sendToAllPlayers(new SUpdateUpgradesPacket(CustomMachinery.UPGRADES.getAllUpgrades()));
                }
            }));
        }
    }
}