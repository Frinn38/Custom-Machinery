package fr.frinn.custommachinery.common.util;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PartialBlockState implements Predicate<CachedBlockInfo> {

    public static final PartialBlockState AIR = new PartialBlockState(Blocks.AIR.getDefaultState(), new ArrayList<>(), null);
    public static final PartialBlockState ANY = new PartialBlockState(Blocks.AIR.getDefaultState(), new ArrayList<>(), null) {
        @Override
        public boolean test(CachedBlockInfo cachedBlockInfo) {
            return true;
        }

        @Override
        public String toString() {
            return "ANY";
        }
    };

    private BlockState blockState;
    private List<Property<?>> properties;
    private CompoundNBT nbt;

    public PartialBlockState(BlockState blockState, List<Property<?>> properties, CompoundNBT nbt) {
        this.blockState = blockState;
        this.properties = properties;
        this.nbt = nbt;
    }

    public PartialBlockState(Block block) {
        this(block.getDefaultState(), new ArrayList<>(), null);
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public List<String> getProperties() {
        return this.properties.stream().map(property -> property.getName() + "=" + this.blockState.get(property)).collect(Collectors.toList());
    }

    public PartialBlockState rotate(Rotation rotation) {
        if(this.blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction direction = this.blockState.get(BlockStateProperties.HORIZONTAL_FACING);
            direction = rotation.rotate(direction);
            BlockState blockState = this.blockState.with(BlockStateProperties.HORIZONTAL_FACING, direction);
            List<Property<?>> properties = Lists.newArrayList(this.properties);
            if(!properties.contains(BlockStateProperties.HORIZONTAL_FACING))
                properties.add(BlockStateProperties.HORIZONTAL_FACING);
            return new PartialBlockState(blockState, properties, this.nbt);
        }
        return this;
    }

    @Override
    public boolean test(CachedBlockInfo cachedBlockInfo) {
        BlockState blockstate = cachedBlockInfo.getBlockState();
        if (!blockstate.matchesBlock(this.blockState.getBlock())) {
            return false;
        } else {
            for(Property<?> property : this.properties) {
                if (blockstate.get(property) != this.blockState.get(property)) {
                    return false;
                }
            }

            if (this.nbt == null) {
                return true;
            } else {
                TileEntity tileentity = cachedBlockInfo.getTileEntity();
                return tileentity != null && NBTUtil.areNBTEquals(this.nbt, tileentity.write(new CompoundNBT()), true);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.blockState.getBlock().getRegistryName());
        if(!this.properties.isEmpty())
            builder.append("[");
        Iterator<Property<?>> iterator = this.properties.iterator();
        while (iterator.hasNext()) {
            Property<?> property = iterator.next();
            Comparable<?> value = this.blockState.get(property);
            builder.append(property.getName());
            builder.append("=");
            builder.append(value);
            if(iterator.hasNext())
                builder.append(",");
            else
                builder.append("]");
        }

        if(this.nbt != null && !this.nbt.isEmpty())
            builder.append(this.nbt);
        return builder.toString();
    }
}
