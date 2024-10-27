package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import fr.frinn.custommachinery.common.util.LootTableHelper.LootData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SLootTablesPacket(Map<ResourceLocation, List<LootData>> loots) implements CustomPacketPayload {

    public static final Type<SLootTablesPacket> TYPE = new Type<>(CustomMachinery.rl("loot_tables"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SLootTablesPacket> CODEC = ByteBufCodecs.map(size -> (Map<ResourceLocation, List<LootData>>)new HashMap<ResourceLocation, List<LootData>>(), ResourceLocation.STREAM_CODEC, ByteBufCodecs.collection(size -> new ArrayList<>(), LootData.STREAM_CODEC)).map(SLootTablesPacket::new, SLootTablesPacket::loots);

    @Override
    public Type<SLootTablesPacket> type() {
        return TYPE;
    }

    public static void handle(SLootTablesPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> LootTableHelper.receiveLoots(packet.loots));
    }
}
