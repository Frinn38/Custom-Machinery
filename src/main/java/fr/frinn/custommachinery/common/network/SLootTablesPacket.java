package fr.frinn.custommachinery.common.network;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SLootTablesPacket(Map<ResourceLocation, List<Pair<ItemStack, Double>>> loots) implements CustomPacketPayload {

    public static final Type<SLootTablesPacket> TYPE = new Type<>(CustomMachinery.rl("loot_tables"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SLootTablesPacket> CODEC = StreamCodec.ofMember(SLootTablesPacket::write, SLootTablesPacket::read);

    @Override
    public Type<SLootTablesPacket> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.loots.size());
        this.loots.forEach((id, loots) -> {
            buf.writeResourceLocation(id);
            buf.writeVarInt(loots.size());
            loots.forEach(pair -> {
                ItemStack.STREAM_CODEC.encode(buf, pair.getFirst());
                buf.writeDouble(pair.getSecond());
            });
        });
    }

    public static SLootTablesPacket read(RegistryFriendlyByteBuf buf) {
        Map<ResourceLocation, List<Pair<ItemStack, Double>>> loots = new HashMap<>();
        int lootSize = buf.readVarInt();
        for(int i = 0; i < lootSize; i++) {
            ResourceLocation id = buf.readResourceLocation();
            List<Pair<ItemStack, Double>> stacks = new ArrayList<>();
            int stackSize = buf.readVarInt();
            for(int j = 0; j < stackSize; j++) {
                stacks.add(Pair.of(ItemStack.STREAM_CODEC.decode(buf), buf.readDouble()));
            }
            loots.put(id, stacks);
        }
        return new SLootTablesPacket(loots);
    }

    public static void handle(SLootTablesPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> LootTableHelper.receiveLoots(packet.loots));
    }
}
