package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.UpgradeLocation;
import fr.frinn.custommachinery.common.util.FileUtils;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collections;
import java.util.Optional;

public record CAddUpgradePacket(String id, Item item, boolean kubejs) implements CustomPacketPayload {

    public static final Type<CAddUpgradePacket> TYPE = new Type<>(CustomMachinery.rl("add_upgrade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CAddUpgradePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CAddUpgradePacket::id,
            ByteBufCodecs.registry(Registries.ITEM),
            CAddUpgradePacket::item,
            ByteBufCodecs.BOOL,
            CAddUpgradePacket::kubejs,
            CAddUpgradePacket::new
    );

    @Override
    public Type<CAddUpgradePacket> type() {
        return TYPE;
    }

    public static void handle(CAddUpgradePacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.getServer() != null && Utils.canPlayerManageMachines(player)) {
            context.enqueueWork(() -> {
                ResourceLocation loc = packet.id.contains(":") ? ResourceLocation.parse(packet.id) : CustomMachinery.rl(packet.id);
                CustomMachinery.LOGGER.info("Player: {} added new upgrade: {}", player.getName().getString(), loc);
                UpgradeLocation location = UpgradeLocation.fromLoader(packet.kubejs ? MachineLocation.Loader.KUBEJS : MachineLocation.Loader.DEFAULT, loc, "", null, null);
                MachineUpgrade newUpgrade = new MachineUpgrade(packet.item, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Optional.empty(), Collections.singletonList(MachineUpgrade.DEFAULT_TOOLTIP), 64);
                FileUtils.writeNewUpgradeJson(player.getServer(), location, newUpgrade, packet.kubejs);
            });
        }
    }
}