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

import javax.annotation.CheckForNull;
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
        BlockPos machinePos = getManager().getTile().getBlockPos();
        box = box.move(machinePos);
        return this.getBlockPos(Order.INCREASING, box, machinePos)
                .map(pos -> new BlockInWorld(getManager().getLevel(), pos, false))
                .filter(block -> filter.stream().flatMap(ingredient -> ingredient.getAll().stream()).anyMatch(state -> state.test(block)) == whitelist)
                .count();
    }

    public boolean placeBlock(AABB box, Order order, PartialBlockState block, int amount) {
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        BlockPos machinePos = getManager().getTile().getBlockPos();
        box = box.move(machinePos);
        if(this.getBlockPos(order, box, machinePos).map(getManager().getLevel()::getBlockState).filter(state -> state.getBlock() == Blocks.AIR).count() < amount)
            return false;
        AtomicInteger toPlace = new AtomicInteger(amount);
        this.getBlockPos(order, box, machinePos).forEach(pos -> {
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
        BlockPos machinePos = getManager().getTile().getBlockPos();
        box = box.move(machinePos);
        AtomicInteger toPlace = new AtomicInteger(amount);
        this.getBlockPos(order, box, machinePos).forEach(pos -> {
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
        BlockPos machinePos = getManager().getTile().getBlockPos();
        box = box.move(machinePos);
        AtomicInteger toPlace = new AtomicInteger(amount);
        this.getBlockPos(order, box, machinePos).forEach(pos -> {
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

    private Stream<BlockPos> getBlockPos(Order order, AABB aabb, BlockPos machinePos) {
        return switch(order) {
            case INCREASING -> StreamSupport.stream(this.betweenClosed(true, Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ)).spliterator(), false);
            case DECREASING -> StreamSupport.stream(this.betweenClosed(false, Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ)).spliterator(), false);
            case RANDOM -> StreamSupport.stream(this.random(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ)).spliterator(), false);
            case NEAREST -> StreamSupport.stream(this.fromCenter(true, machinePos, Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ)).spliterator(), false);
            case FURTHEST -> StreamSupport.stream(this.fromCenter(false, machinePos, Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ)).spliterator(), false);
        };
    }

    //Increasing false: top to bottom, true: bottom to top
    private Iterable<BlockPos> betweenClosed(boolean increasing, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int xSize = maxX - minX + 1;
        int ySize = maxY - minY + 1;
        int zSize = maxZ - minZ + 1;
        int blockAmount = xSize * ySize * zSize;
        return () -> new AbstractIterator<>() {
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int index = increasing ? blockAmount - 1 : 0;

            @Override
            @CheckForNull
            protected BlockPos computeNext() {
                if(this.index < 0 || this.index >= blockAmount) {
                    return this.endOfData();
                } else {
                    int zPos = this.index % zSize;
                    int j1 = this.index / zSize;
                    int xPos = j1 % xSize;
                    int yPos = j1 / xSize;
                    this.index += increasing ? -1 : 1;
                    return this.cursor.set(minX + xPos, minY + yPos, minZ + zPos);
                }
            }
        };
    }

    private Iterable<BlockPos> random(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int xSize = maxX - minX + 1;
        int ySize = maxY - minY + 1;
        int zSize = maxZ - minZ + 1;
        int blockAmount = xSize * ySize * zSize;
        List<Integer> indexes = IntStream.rangeClosed(0, blockAmount - 1).boxed().collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(indexes);
        return () -> new AbstractIterator<>() {
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int index;

            @Override
            @CheckForNull
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

    //Nearest false: furthest to nearest, true: nearest to furthest
    private Iterable<BlockPos> fromCenter(boolean nearest, BlockPos center, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        long dx = Math.max(Math.abs((long) minX - center.getX()), Math.abs((long) maxX - center.getX()));
        long dy = Math.max(Math.abs((long) minY - center.getY()), Math.abs((long) maxY - center.getY()));
        long dz = Math.max(Math.abs((long) minZ - center.getZ()), Math.abs((long) maxZ - center.getZ()));
        long maxDistanceSqrt = dx * dx + dy * dy + dz * dz;

        return () -> new AbstractIterator<>() {
            private final MutableBlockPos cursor = new MutableBlockPos();
            private long distanceSqr = nearest ? 0 : maxDistanceSqrt;
            private int x = minX;
            private int y = minY;
            private int z = minZ;

            @Override
            @CheckForNull
            protected BlockPos computeNext() {
                while (nearest ? distanceSqr <= maxDistanceSqrt : distanceSqr >= 0) {
                    while (x <= maxX) {
                        while (y <= maxY) {
                            while (z <= maxZ) {
                                int currentX = x;
                                int currentY = y;
                                int currentZ = z++;

                                long dx = (long) currentX - center.getX();
                                long dy = (long) currentY - center.getY();
                                long dz = (long) currentZ - center.getZ();

                                if (dx * dx + dy * dy + dz * dz == distanceSqr)
                                    return this.cursor.set(currentX, currentY, currentZ);
                            }
                            z = minZ;
                            y++;
                        }
                        y = minY;
                        x++;
                    }

                    // Restart the bounding-box scan for the next distance
                    if(nearest)
                        distanceSqr++;
                    else
                        distanceSqr--;
                    x = minX;
                    y = minY;
                    z = minZ;
                }
                return this.endOfData();
            }
        };
    }
}
