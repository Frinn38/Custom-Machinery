package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockMachineComponent extends AbstractMachineComponent {

    public BlockMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.BOTH);
    }

    @Override
    public MachineComponentType<BlockMachineComponent> getType() {
        return Registration.BLOCK_MACHINE_COMPONENT.get();
    }

    public long getBlockAmount(AABB box, List<IIngredient<PartialBlockState>> filter, boolean whitelist) {
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        box = box.move(getManager().getTile().getBlockPos());
        return BlockPos.betweenClosedStream(box)
                .map(pos -> new BlockInWorld(getManager().getLevel(), pos, false))
                .filter(block -> filter.stream().flatMap(ingredient -> ingredient.getAll().stream()).anyMatch(state -> state.test(block)) == whitelist)
                .count();
    }

    public boolean placeBlock(AABB box, PartialBlockState block, int amount) {
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        box = box.move(getManager().getTile().getBlockPos());
        if(BlockPos.betweenClosedStream(box).map(getManager().getLevel()::getBlockState).filter(state -> state.getBlock() == Blocks.AIR).count() < amount)
            return false;
        AtomicInteger toPlace = new AtomicInteger(amount);
        BlockPos.betweenClosedStream(box).forEach(pos -> {
            if(toPlace.get() > 0 && getManager().getLevel().getBlockState(pos).getBlock() == Blocks.AIR) {
                setBlock(getManager().getLevel(), pos, block);
                toPlace.addAndGet(-1);
            }
        });
        return true;
    }

    public boolean replaceBlock(AABB box, PartialBlockState block, int amount, boolean drop, List<IIngredient<PartialBlockState>> filter, boolean whitelist) {
        if(getBlockAmount(box, filter, whitelist) < amount)
            return false;
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        box = box.move(getManager().getTile().getBlockPos());
        AtomicInteger toPlace = new AtomicInteger(amount);
        BlockPos.betweenClosedStream(box).forEach(pos -> {
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

    public boolean breakBlock(AABB box, List<IIngredient<PartialBlockState>> filter, boolean whitelist, int amount, boolean drop) {
        if(getBlockAmount(box, filter, whitelist) < amount)
            return false;
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        box = box.move(getManager().getTile().getBlockPos());
        AtomicInteger toPlace = new AtomicInteger(amount);
        BlockPos.betweenClosedStream(box).forEach(pos -> {
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
            tile.load(nbt);
        }
    }
}
