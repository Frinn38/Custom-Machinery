package fr.frinn.custommachinery.common.util;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class Ingredient<T> implements Predicate<T> {

    private ITag<T> tag;
    private T object;

    public Ingredient(T object) {
        this.object = object;
    }

    public Ingredient(ITag<T> tag) {
        this.tag = tag;
    }

    @Nullable
    public T getObject() {
        return this.object;
    }

    @Nullable
    public ITag<T> getTag() {
        return this.tag;
    }

    public List<T> getAll() {
        if(this.object != null)
            return Collections.singletonList(this.object);
        if(this.tag != null)
            return Lists.newArrayList(tag.getAllElements());
        throw new IllegalStateException("Invalid ingredient: " + this.getClass().toGenericString());
    }

    public static class ItemIngredient extends Ingredient<Item> {

        public static final Codec<ItemIngredient> CODEC = Codec.STRING.flatXmap(encoded -> {
            boolean isTag = encoded.startsWith("#");
            try {
                ResourceLocation location = new ResourceLocation(encoded.substring(isTag ? 1 : 0));
                if(isTag)
                    return DataResult.success(new ItemIngredient(TagCollectionManager.getManager().getItemTags().get(location)));
                return DataResult.success(new ItemIngredient(ForgeRegistries.ITEMS.getValue(location)));
            } catch (ResourceLocationException e) {
                return DataResult.error(e.getMessage());
            }
        }, itemIngredient -> {
            if(itemIngredient.getObject() != null && itemIngredient.getObject().getRegistryName() != null)
                return DataResult.success(itemIngredient.getObject().getRegistryName().toString());
            if(itemIngredient.getTag() != null)
                return DataResult.success("#" + Utils.getItemTagID(itemIngredient.getTag()));
            return DataResult.error("ItemIngredient with no item or tag");
        });

        public ItemIngredient(Item object) {
            super(object);
        }

        public ItemIngredient(ITag<Item> tag) {
            super(tag);
        }

        @Override
        public boolean test(Item toTest) {
            return getAll().contains(toTest);
        }

        @Override
        public String toString() {
            if(this.getObject() != null && this.getObject().getRegistryName() != null)
                return this.getObject().getRegistryName().toString();
            if(this.getTag() != null)
                return "#" + Utils.getItemTagID(this.getTag());
            throw new IllegalStateException("ItemIngredient with no item or tag");
        }
    }

    public static class FluidIngredient extends Ingredient<Fluid> {

        public static final Codec<FluidIngredient> CODEC = Codec.STRING.flatXmap(encoded -> {
            boolean isTag = encoded.startsWith("#");
            try {
                ResourceLocation location = new ResourceLocation(encoded.substring(isTag ? 1 : 0));
                if(isTag)
                    return DataResult.success(new FluidIngredient(TagCollectionManager.getManager().getFluidTags().get(location)));
                return DataResult.success(new FluidIngredient(ForgeRegistries.FLUIDS.getValue(location)));
            } catch (ResourceLocationException e) {
                return DataResult.error(e.getMessage());
            }
        }, fluidIngredient -> {
            if(fluidIngredient.getObject() != null && fluidIngredient.getObject().getRegistryName() != null)
                return DataResult.success(fluidIngredient.getObject().getRegistryName().toString());
            if(fluidIngredient.getTag() != null)
                return DataResult.success("#" + Utils.getFluidTagID(fluidIngredient.getTag()));
            return DataResult.error("FluidIngredient with no item or tag");
        });

        public FluidIngredient(Fluid object) {
            super(object);
        }

        public FluidIngredient(ITag<Fluid> tag) {
            super(tag);
        }

        @Override
        public boolean test(Fluid toTest) {
            return getAll().contains(toTest);
        }

        @Override
        public String toString() {
            if(this.getObject() != null && this.getObject().getRegistryName() != null)
                return this.getObject().getRegistryName().toString();
            if(this.getTag() != null)
                return "#" + Utils.getFluidTagID(this.getTag());
            throw new IllegalStateException("FluidIngredient with no item or tag");
        }
    }

    public static class BlockIngredient extends Ingredient<Block> {

        public static final Codec<BlockIngredient> CODEC = Codec.STRING.flatXmap(encoded -> {
            boolean isTag = encoded.startsWith("#");
            try {
                ResourceLocation location = new ResourceLocation(encoded.substring(isTag ? 1 : 0));
                if(isTag)
                    return DataResult.success(new BlockIngredient(TagCollectionManager.getManager().getBlockTags().get(location)));
                return DataResult.success(new BlockIngredient(ForgeRegistries.BLOCKS.getValue(location)));
            } catch (ResourceLocationException e) {
                return DataResult.error(e.getMessage());
            }
        }, blockIngredient -> {
            if(blockIngredient.getObject() != null && blockIngredient.getObject().getRegistryName() != null)
                return DataResult.success(blockIngredient.getObject().getRegistryName().toString());
            if(blockIngredient.getTag() != null)
                return DataResult.success("#" + Utils.getBlockTagID(blockIngredient.getTag()));
            return DataResult.error("BlockIngredient with no item or tag");
        });

        public BlockIngredient(Block object) {
            super(object);
        }

        public BlockIngredient(ITag<Block> tag) {
            super(tag);
        }

        @Override
        public boolean test(Block toTest) {
            return getAll().contains(toTest);
        }

        @Override
        public String toString() {
            if(this.getObject() != null && this.getObject().getRegistryName() != null)
                return this.getObject().getRegistryName().toString();
            if(this.getTag() != null)
                return "#" + Utils.getBlockTagID(this.getTag());
            throw new IllegalStateException("BlockIngredient with no item or tag");
        }
    }
}
