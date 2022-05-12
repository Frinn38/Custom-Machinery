package fr.frinn.custommachinery.common.network;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SLootTablesPacket {

    private final Map<ResourceLocation, List<Pair<ItemStack, Double>>> loots;

    public SLootTablesPacket(Map<ResourceLocation, List<Pair<ItemStack, Double>>> loots) {
        this.loots = loots;
    }

    public static void encode(SLootTablesPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.loots.size());
        pkt.loots.forEach((id, loots) -> {
            buf.writeResourceLocation(id);
            buf.writeVarInt(loots.size());
            loots.forEach(pair -> {
                buf.writeItemStack(pair.getFirst(), false);
                buf.writeDouble(pair.getSecond());
            });
        });
    }

    public static SLootTablesPacket decode(FriendlyByteBuf buf) {
        Map<ResourceLocation, List<Pair<ItemStack, Double>>> loots = new HashMap<>();
        int lootSize = buf.readVarInt();
        for(int i = 0; i < lootSize; i++) {
            ResourceLocation id = buf.readResourceLocation();
            List<Pair<ItemStack, Double>> stacks = new ArrayList<>();
            int stackSize = buf.readVarInt();
            for(int j = 0; j < stackSize; j++) {
                stacks.add(Pair.of(buf.readItem(), buf.readDouble()));
            }
            loots.put(id, stacks);
        }
        return new SLootTablesPacket(loots);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
            context.get().enqueueWork(() -> LootTableHelper.receiveLoots(this.loots));
        context.get().setPacketHandled(true);
    }
}
