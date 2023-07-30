package fr.frinn.custommachinery.common.network;

import com.mojang.datafixers.util.Pair;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SLootTablesPacket extends BaseS2CMessage {

    private final Map<ResourceLocation, List<Pair<ItemStack, Double>>> loots;

    public SLootTablesPacket(Map<ResourceLocation, List<Pair<ItemStack, Double>>> loots) {
        this.loots = loots;
    }

    @Override
    public MessageType getType() {
        return PacketManager.LOOT_TABLES;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.loots.size());
        this.loots.forEach((id, loots) -> {
            buf.writeResourceLocation(id);
            buf.writeVarInt(loots.size());
            loots.forEach(pair -> {
                buf.writeItem(pair.getFirst());
                buf.writeDouble(pair.getSecond());
            });
        });
    }

    public static SLootTablesPacket read(FriendlyByteBuf buf) {
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

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnvironment() == Env.CLIENT)
            context.queue(() -> LootTableHelper.receiveLoots(this.loots));
    }
}
