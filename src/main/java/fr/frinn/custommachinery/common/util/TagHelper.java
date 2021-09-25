package fr.frinn.custommachinery.common.util;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class TagHelper {

    private static final Map<ResourceLocation, ITag.INamedTag<Item>> itemTags = new HashMap<>();
    private static final Map<ResourceLocation, ITag.INamedTag<Fluid>> fluidTags = new HashMap<>();
    private static final Map<ResourceLocation, ITag.INamedTag<Block>> blockTags = new HashMap<>();

    public static ITag.INamedTag<Item> getItemTag(ResourceLocation loc) {
        return itemTags.computeIfAbsent(loc, ItemTags::createOptional);
    }

    public static ITag.INamedTag<Fluid> getFluidTag(ResourceLocation loc) {
        return fluidTags.computeIfAbsent(loc, FluidTags::createOptional);
    }

    public static ITag.INamedTag<Block> getBlockTag(ResourceLocation loc) {
        return blockTags.computeIfAbsent(loc, BlockTags::createOptional);
    }
}
