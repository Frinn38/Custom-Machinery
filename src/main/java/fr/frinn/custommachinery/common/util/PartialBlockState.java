package fr.frinn.custommachinery.common.util;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class PartialBlockState implements Predicate<BlockInWorld> {

    public static final PartialBlockState AIR = new PartialBlockState(Blocks.AIR.defaultBlockState(), new ArrayList<>(), null);
    public static final PartialBlockState ANY = new PartialBlockState(Blocks.AIR.defaultBlockState(), new ArrayList<>(), null) {
        @Override
        public boolean test(BlockInWorld cachedBlockInfo) {
            return true;
        }

        @Override
        public String toString() {
            return "ANY";
        }
    };

    private BlockState blockState;
    private List<Property<?>> properties;
    private CompoundTag nbt;

    public PartialBlockState(BlockState blockState, List<Property<?>> properties, CompoundTag nbt) {
        this.blockState = blockState;
        this.properties = properties;
        this.nbt = nbt;
    }

    public PartialBlockState(Block block) {
        this(block.defaultBlockState(), new ArrayList<>(), null);
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public List<String> getProperties() {
        return this.properties.stream().map(property -> property.getName() + "=" + this.blockState.getValue(property)).toList();
    }

    public CompoundTag getNbt() {
        return this.nbt;
    }

    public PartialBlockState rotate(Rotation rotation) {
        if(this.blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && this.blockState.getBlock() != Registration.CUSTOM_MACHINE_BLOCK.get()) {
            Direction direction = this.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            direction = rotation.rotate(direction);
            BlockState blockState = this.blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
            List<Property<?>> properties = Lists.newArrayList(this.properties);
            if(!properties.contains(BlockStateProperties.HORIZONTAL_FACING))
                properties.add(BlockStateProperties.HORIZONTAL_FACING);
            return new PartialBlockState(blockState, properties, this.nbt);
        }
        return this;
    }

    @Override
    public boolean test(BlockInWorld cachedBlockInfo) {
        BlockState blockstate = cachedBlockInfo.getState();
        if (!blockstate.is(this.blockState.getBlock())) {
            return false;
        } else {
            for(Property<?> property : this.properties) {
                if (blockstate.getValue(property) != this.blockState.getValue(property)) {
                    return false;
                }
            }

            if (this.nbt == null) {
                return true;
            } else {
                BlockEntity tileentity = cachedBlockInfo.getEntity();
                return tileentity != null && NbtUtils.compareNbt(this.nbt, tileentity.saveWithFullMetadata(), true);
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
            Comparable<?> value = this.blockState.getValue(property);
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

    public MutableComponent getName() {
        return new TranslatableComponent(this.blockState.getBlock().getDescriptionId());
    }

    public ResourceLocation getModelLocation() {
        ResourceLocation location = this.blockState.getBlock().getRegistryName();
        if(location == null)
            throw new IllegalStateException("Can't get location of a null block");
        StringBuilder stringbuilder = new StringBuilder();

        for(Map.Entry<Property<?>, Comparable<?>> entry : this.getBlockState().getValues().entrySet()) {
            if (stringbuilder.length() != 0)
                stringbuilder.append(',');

            Property<?> property = entry.getKey();
            stringbuilder.append(property.getName());
            stringbuilder.append('=');
            stringbuilder.append(getPropertyValueString(property, entry.getValue()));
        }

        String properties = stringbuilder.toString();
        return new ModelResourceLocation(location, properties);
    }

    private static <T extends Comparable<T>> String getPropertyValueString(Property<T> property, Comparable<?> value) {
        return property.getName((T)value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartialBlockState)) return false;
        PartialBlockState other = (PartialBlockState) o;
        if(this.blockState != other.blockState)
            return false;
        if(!this.properties.containsAll(other.properties) || !other.properties.containsAll(this.properties))
            return false;
        return NbtUtils.compareNbt(this.nbt, other.nbt, true);
    }
}
