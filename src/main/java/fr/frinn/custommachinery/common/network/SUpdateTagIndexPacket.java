package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.common.util.TagIndex;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;
import java.util.function.Supplier;

/**
 * Purpose of this packet to populate TagIndex before recipes sync.
 * Stupid mojang sending recipes first and than tags, like wtf??
 * Also there's no event so early, so it's sent from mixin injections...
 */
public class SUpdateTagIndexPacket {
    private final Map<ResourceLocation, ITag.INamedTag<Item>> itemTagIndex;
    private final Map<ResourceLocation, ITag.INamedTag<Fluid>> fluidTagIndex;
    private final Map<ResourceLocation, ITag.INamedTag<Block>> blockTagIndex;

    public SUpdateTagIndexPacket(
            Map<ResourceLocation, ITag.INamedTag<Item>> itemTagIndex,
            Map<ResourceLocation, ITag.INamedTag<Fluid>> fluidTagIndex,
            Map<ResourceLocation, ITag.INamedTag<Block>> blockTagIndex
    ) {
        this.itemTagIndex = new HashMap<>(itemTagIndex);
        this.fluidTagIndex = new HashMap<>(fluidTagIndex);
        this.blockTagIndex = new HashMap<>(blockTagIndex);
    }

    public static void encode(SUpdateTagIndexPacket pkt, PacketBuffer buf) {
        writeTagIndex(buf, pkt.itemTagIndex);
        writeTagIndex(buf, pkt.fluidTagIndex);
        writeTagIndex(buf, pkt.blockTagIndex);
    }

    private static <V extends IForgeRegistryEntry<V>> void writeTagIndex(PacketBuffer buf, Map<ResourceLocation, ITag.INamedTag<V>> tagIndex) {
        buf.writeInt(tagIndex.size());
        tagIndex.entrySet().forEach(e -> writeTag(buf, e));
    }

    private static <V extends IForgeRegistryEntry<V>> void writeTag(PacketBuffer buf, Map.Entry<ResourceLocation, ITag.INamedTag<V>> tagEntry) {
        buf.writeResourceLocation(tagEntry.getKey());
        List<V> items = tagEntry.getValue().getAllElements();
        buf.writeInt(items.size());
        for (V item : items) {
            buf.writeResourceLocation(item.getRegistryName());
        }
    }

    public static SUpdateTagIndexPacket decode(PacketBuffer buf) {
        Map<ResourceLocation, ITag.INamedTag<Item>> itemTagIndex = readTagIndex(buf, ForgeRegistries.ITEMS);
        Map<ResourceLocation, ITag.INamedTag<Fluid>> fluidTagIndex = readTagIndex(buf, ForgeRegistries.FLUIDS);
        Map<ResourceLocation, ITag.INamedTag<Block>> blockTagIndex = readTagIndex(buf, ForgeRegistries.BLOCKS);
        return new SUpdateTagIndexPacket(itemTagIndex, fluidTagIndex, blockTagIndex);
    }

    public static <V extends IForgeRegistryEntry<V>> Map<ResourceLocation, ITag.INamedTag<V>> readTagIndex(PacketBuffer buf, IForgeRegistry<V> registry) {
        Map<ResourceLocation, ITag.INamedTag<V>> tagIndex = new HashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            ITag.INamedTag<V> tag = readTag(buf, registry);
            tagIndex.put(tag.getName(), tag);
        }
        return tagIndex;
    }

    public static <V extends IForgeRegistryEntry<V>> ITag.INamedTag<V> readTag(PacketBuffer buf, IForgeRegistry<V> registry) {
        ResourceLocation tagName = buf.readResourceLocation();
        int size = buf.readInt();
        Set<V> tagContent = new HashSet<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation contentId = buf.readResourceLocation();
            V value = registry.getValue(contentId);
            tagContent.add(value);
        }
        ITag<V> tag = Tag.getTagFromContents(tagContent);
        return TagIndex.nameTag(tagName, tag);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> {
                TagIndex.setNewTagIndex(itemTagIndex, "item");
                TagIndex.setNewTagIndex(fluidTagIndex, "fluid");
                TagIndex.setNewTagIndex(blockTagIndex, "block");
            });
        }
        context.get().setPacketHandled(true);
    }
}
