package fr.frinn.custommachinery.common.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.stream.Stream;

public class TagUtil {

    public static Stream<Item> getItems(TagKey<Item> tag) {
        ITagManager<Item> manager = ForgeRegistries.ITEMS.tags();
        if(manager == null)
            throw new IllegalStateException("Item registry doesn't support tags... something went very wrong");
        return manager.getTag(tag).stream();
    }

    public static Stream<Block> getBlocks(TagKey<Block> tag) {
        ITagManager<Block> manager = ForgeRegistries.BLOCKS.tags();
        if(manager == null)
            throw new IllegalStateException("Block registry doesn't support tags... something went very wrong");
        return manager.getTag(tag).stream();
    }

    public static Stream<Fluid> getFluids(TagKey<Fluid> tag) {
        ITagManager<Fluid> manager = ForgeRegistries.FLUIDS.tags();
        if(manager == null)
            throw new IllegalStateException("Fluid registry doesn't support tags... something went very wrong");
        return manager.getTag(tag).stream();
    }
}
