package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.common.network.SUpdateTagIndexPacket;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class TagIndex {

    public static ResourceLocation getItemTagID(ITag.INamedTag<Item> tag) {
        return tag.getName();
    }

    public static ResourceLocation getFluidTagID(ITag.INamedTag<Fluid> tag) {
        return tag.getName();
    }

    public static ResourceLocation getBlockTagID(ITag.INamedTag<Block> tag) {
        return tag.getName();
    }

    public static ITag.INamedTag<Item> getItemTag(ResourceLocation loc) {
        ITag<Item> genericTag = itemTagIndex.get(loc);
        genericTag = genericTag != null ? genericTag : ItemTags.getCollection().get(loc);
        if (genericTag == null) return null;
        return new DelegatedNamedTag<>(loc, genericTag);
    }

    public static ITag.INamedTag<Fluid> getFluidTag(ResourceLocation loc) {
        ITag<Fluid> genericTag = fluidTagIndex.get(loc);
        genericTag = genericTag != null ? genericTag : FluidTags.getCollection().get(loc);
        if (genericTag == null) return null;
        return new DelegatedNamedTag<>(loc, genericTag);
    }

    public static ITag.INamedTag<Block> getBlockTag(ResourceLocation loc) {
        ITag<Block> genericTag = blockTagIndex.get(loc);
        genericTag = genericTag != null ? genericTag : BlockTags.getCollection().get(loc);
        if (genericTag == null) return null;
        return new DelegatedNamedTag<>(loc, genericTag);
    }

    private static Map<ResourceLocation, ITag.INamedTag<Item>> itemTagIndex = new HashMap<>();
    private static Map<ResourceLocation, ITag.INamedTag<Fluid>> fluidTagIndex = new HashMap<>();
    private static Map<ResourceLocation, ITag.INamedTag<Block>> blockTagIndex = new HashMap<>();

    public static SUpdateTagIndexPacket createUpdatePacket() {
        return new SUpdateTagIndexPacket(itemTagIndex, fluidTagIndex, blockTagIndex);
    }

    public static <T extends ITag<V>, V> void setNewTagIndex(Map<ResourceLocation, T> tagMap, String tagType) {
        switch (tagType) {
            case "item":
                itemTagIndex = unsafeCast(wrapWithDelegates(tagMap));
                break;
            case "fluid":
                fluidTagIndex = unsafeCast(wrapWithDelegates(tagMap));
                break;
            case "block":
                blockTagIndex = unsafeCast(wrapWithDelegates(tagMap));
                break;
        }
    }

    private static <T extends ITag<V>, V> Map<ResourceLocation, ITag.INamedTag<V>> wrapWithDelegates(Map<ResourceLocation, T> tagMap) {
        return tagMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new DelegatedNamedTag<>(
                        entry.getKey(),
                        entry.getValue()
                )
        ));
    }

    @SuppressWarnings("unchecked")
    private static <T, R> R unsafeCast(T t) {
        return (R) t;
    }

    public static <T> ITag.INamedTag<T> nameTag(ResourceLocation name, ITag<T> tag) {
        return new DelegatedNamedTag<>(name, tag);
    }

    private static class DelegatedNamedTag<T> implements ITag.INamedTag<T> {

        private final ResourceLocation name;
        private final ITag<T> delegate;

        public DelegatedNamedTag(ResourceLocation name, ITag<T> delegate) {
            if (delegate == null)
                throw new NullPointerException("DelegatedNamedTag: delegate == null");
            this.name = name;
            this.delegate = delegate;
        }

        @Override
        public boolean contains(T element) {
            return delegate.contains(element);
        }

        @Override
        public List<T> getAllElements() {
            return delegate.getAllElements();
        }

        @Override
        public ResourceLocation getName() {
            return name;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            if (!(obj instanceof INamedTag)) return false;
            INamedTag<T> tag = (INamedTag<T>) obj;
            return delegate.equals(tag);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name.toString();
        }
    }
}
