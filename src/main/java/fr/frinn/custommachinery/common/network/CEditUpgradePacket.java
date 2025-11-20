package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.UpgradeLocation;
import fr.frinn.custommachinery.common.util.FileUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

public record CEditUpgradePacket(UpgradeLocation location, MachineUpgrade upgrade) implements CustomPacketPayload {

    public static final Type<CEditUpgradePacket> TYPE = new Type<>(CustomMachinery.rl("edit_upgrade"));

    public static final StreamCodec<FriendlyByteBuf, CEditUpgradePacket> CODEC = new StreamCodec<>() {
        @Override
        public CEditUpgradePacket decode(FriendlyByteBuf buf) {
            UpgradeLocation location = UpgradeLocation.CODEC.fromNetwork(buf);
            MachineUpgrade upgrade = MachineUpgrade.CODEC.fromNetwork(buf);
            return new CEditUpgradePacket(location, upgrade);
        }

        @Override
        public void encode(FriendlyByteBuf buf, CEditUpgradePacket packet) {
            UpgradeLocation.CODEC.toNetwork(packet.location, buf);
            MachineUpgrade.CODEC.toNetwork(packet.upgrade, buf);
        }
    };

    @Override
    public Type<CEditUpgradePacket> type() {
        return TYPE;
    }

    public static void handle(CEditUpgradePacket packet, IPayloadContext context) {
        if(context.player() instanceof ServerPlayer player && player.hasPermissions(2)) {
            context.enqueueWork(() -> {
                FileUtils.writeUpgradeJson(player.server, packet.location, packet.upgrade);
                CustomMachinery.UPGRADES.removeUpgrade(packet.location.id());
                UpgradeLocation location = packet.location;
                try {
                    File upgradeJson = location.getFile(player.server);
                    if(upgradeJson != null && upgradeJson.exists()) {
                        BasicFileAttributes attributes = Files.getFileAttributeView(upgradeJson.toPath(), BasicFileAttributeView.class).readAttributes();
                        location = UpgradeLocation.fromLoader(location.loader(), location.id(), location.packName(), attributes.creationTime(), attributes.lastModifiedTime());
                    }
                } catch (IOException | NullPointerException ignored) {

                }
                CustomMachinery.UPGRADES.addUpgrade(location, packet.upgrade);
                PacketDistributor.sendToAllPlayers(new SUpdateUpgradesPacket(CustomMachinery.UPGRADES.getAllUpgrades()));
            });
        }
    }
}
