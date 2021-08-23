package fr.frinn.custommachinery.common.network;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SLootTablesPacket {

    private Map<ResourceLocation, List<Pair<ItemStack, Float>>> loots;

    public SLootTablesPacket(Map<ResourceLocation, List<Pair<ItemStack, Float>>> loots) {
        this.loots = loots;
    }

    public static void encode(SLootTablesPacket pkt, PacketBuffer buf) {
        buf.writeVarInt(pkt.loots.size());
        pkt.loots.forEach((id, loots) -> {
            buf.writeResourceLocation(id);
            buf.writeVarInt(loots.size());
            loots.forEach(pair -> {
                buf.writeItemStack(pair.getFirst(), false);
                buf.writeFloat(pair.getSecond());
            });
        });
    }

    public static SLootTablesPacket decode(PacketBuffer buf) {
        Map<ResourceLocation, List<Pair<ItemStack, Float>>> loots = new HashMap<>();
        int lootSize = buf.readVarInt();
        for(int i = 0; i < lootSize; i++) {
            ResourceLocation id = buf.readResourceLocation();
            List<Pair<ItemStack, Float>> stacks = new ArrayList<>();
            int stackSize = buf.readVarInt();
            for(int j = 0; j < stackSize; j++) {
                stacks.add(Pair.of(buf.readItemStack(), buf.readFloat()));
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
