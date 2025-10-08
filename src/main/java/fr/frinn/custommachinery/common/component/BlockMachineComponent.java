package fr.frinn.custommachinery.common.component;

import com.google.common.collect.AbstractIterator;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.requirement.BlockRequirement.Order;
import fr.frinn.custommachinery.common.util.BlockIngredient;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BlockMachineComponent extends AbstractMachineComponent {

    public BlockMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.BOTH);
    }

    @Override
    public MachineComponentType<BlockMachineComponent> getType() {
        return Registration.BLOCK_MACHINE_COMPONENT.get();
    }

    public long getBlockAmount(AABB box, List<BlockIngredient> filter, boolean whitelist) {
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        box = box.move(getManager().getTile().getBlockPos());
        return this.getBlockPos(Order.INCREASING, box)
                .map(pos -> new BlockInWorld(getManager().getLevel(), pos, false))
                .filter(block -> filter.stream().flatMap(ingredient -> ingredient.getAll().stream()).anyMatch(state -> state.test(block)) == whitelist)
                .count();
    }

    public boolean placeBlock(AABB box, Order order, PartialBlockState block, int amount) {
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        box = box.move(getManager().getTile().getBlockPos());
        if(this.getBlockPos(order, box).map(getManager().getLevel()::getBlockState).filter(state -> state.getBlock() == Blocks.AIR).count() < amount)
            return false;
        AtomicInteger toPlace = new AtomicInteger(amount);
        this.getBlockPos(order, box).forEach(pos -> {
            if(toPlace.get() > 0 && getManager().getLevel().getBlockState(pos).getBlock() == Blocks.AIR) {
                setBlock(getManager().getLevel(), pos, block);
                toPlace.addAndGet(-1);
            }
        });
        return true;
    }

    public boolean replaceBlock(AABB box, Order order, PartialBlockState block, int amount, boolean drop, List<BlockIngredient> filter, boolean whitelist) {
        if(getBlockAmount(box, filter, whitelist) < amount)
            return false;
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        box = box.move(getManager().getTile().getBlockPos());
        AtomicInteger toPlace = new AtomicInteger(amount);
        this.getBlockPos(order, box).forEach(pos -> {
            if(toPlace.get() > 0) {
                BlockInWorld cached = new BlockInWorld(getManager().getLevel(), pos, false);
                if(filter.stream().flatMap(ingredient -> ingredient.getAll().stream()).anyMatch(state -> state.test(cached)) == whitelist) {
                    if(!cached.getState().isAir())
                        getManager().getLevel().destroyBlock(pos, drop);
                    setBlock(getManager().getLevel(), pos, block);
                    toPlace.addAndGet(-1);
                }
            }
        });
        return true;
    }

    public boolean breakBlock(AABB box, Order order, List<BlockIngredient> filter, boolean whitelist, int amount, boolean drop) {
        if(getBlockAmount(box, filter, whitelist) < amount)
            return false;
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        box = box.move(getManager().getTile().getBlockPos());
        AtomicInteger toPlace = new AtomicInteger(amount);
        this.getBlockPos(order, box).forEach(pos -> {
            if(toPlace.get() > 0) {
                BlockInWorld cached = new BlockInWorld(getManager().getLevel(), pos, false);
                if(filter.stream().flatMap(ingredient -> ingredient.getAll().stream()).anyMatch(state -> state.test(cached)) == whitelist) {
                    if(!cached.getState().isAir())
                        getManager().getLevel().destroyBlock(pos, drop);
                    toPlace.addAndGet(-1);
                }
            }
        });
        return true;
    }

    private void setBlock(Level world, BlockPos pos, PartialBlockState state) {
        world.setBlockAndUpdate(pos, state.getBlockState());
        BlockEntity tile = world.getBlockEntity(pos);
        if(tile != null && state.getNbt() != null && !state.getNbt().isEmpty()) {
            CompoundTag nbt = state.getNbt().copy();
            nbt.putInt("x", pos.getX());
            nbt.putInt("y", pos.getY());
            nbt.putInt("z", pos.getZ());
            tile.loadWithComponents(nbt, world.registryAccess());
        }
    }

    private Stream<BlockPos> getBlockPos(Order order, AABB aabb) {
        return switch(order) {
            case INCREASING -> StreamSupport.stream(this.betweenClosed(true, Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ)).spliterator(), false);
            case DECREASING -> StreamSupport.stream(this.betweenClosed(false, Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ)).spliterator(), false);
            case RANDOM -> StreamSupport.stream(this.random(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ)).spliterator(), false);
        };
    }

    //Increasing false: top to bottom, true:bottom to top
    private Iterable<BlockPos> betweenClosed(boolean increasing, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int xSize = maxX - minX + 1;
        int ySize = maxY - minY + 1;
        int zSize = maxZ - minZ + 1;
        int blockAmount = xSize * ySize * zSize;
        return () -> new AbstractIterator<>() {
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int index = increasing ? blockAmount : 0;

            protected BlockPos computeNext() {
                if (this.index == (increasing ? 0 : blockAmount)) {
                    return this.endOfData();
                } else {
                    int zPos = this.index % zSize;
                    int j1 = this.index / zSize;
                    int xPos = j1 % xSize;
                    int yPos = j1 / xSize;
                    this.index = increasing ? this.index - 1 : this.index + 1;
                    return this.cursor.set(maxX - xPos, maxY - yPos, maxZ - zPos);
                }
            }
        };
    }

    private Iterable<BlockPos> random(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int xSize = maxX - minX + 1;
        int ySize = maxY - minY + 1;
        int zSize = maxZ - minZ + 1;
        int blockAmount = xSize * ySize * zSize;
        List<Integer> indexes = IntStream.rangeClosed(0, blockAmount).boxed().collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(indexes);
        return () -> new AbstractIterator<>() {
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int index;

            protected BlockPos computeNext() {
                if (this.index == blockAmount) {
                    return this.endOfData();
                } else {
                    int blockIndex = indexes.get(this.index);
                    int zPos = blockIndex % zSize;
                    int j1 = blockIndex / zSize;
                    int xPos = j1 % xSize;
                    int yPos = j1 / xSize;
                    this.index++;
                    return this.cursor.set(maxX - xPos, maxY - yPos, maxZ - zPos);
                }
            }
        };
    }
}
