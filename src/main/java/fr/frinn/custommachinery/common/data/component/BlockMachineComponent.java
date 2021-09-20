package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.components.ComponentIOMode;
import fr.frinn.custommachinery.api.components.IMachineComponentManager;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

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

    public long getBlockAmount(AxisAlignedBB box, List<IIngredient<PartialBlockState>> filter, boolean whitelist) {
        if(getManager().getTile().getWorld() == null)
            return 0;
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
        box = box.offset(getManager().getTile().getPos());
        return BlockPos.getAllInBox(box)
                .map(pos -> new CachedBlockInfo(getManager().getTile().getWorld(), pos, false))
                .filter(block -> filter.stream().flatMap(ingredient -> ingredient.getAll().stream()).anyMatch(state -> state.test(block)) == whitelist)
                .count();
    }

    public boolean placeBlock(AxisAlignedBB box, PartialBlockState block, int amount) {
        if(getManager().getTile().getWorld() == null)
            return false;
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
        box = box.offset(getManager().getTile().getPos());
        if(BlockPos.getAllInBox(box).map(getManager().getTile().getWorld()::getBlockState).filter(state -> state.getBlock() == Blocks.AIR).count() < amount)
            return false;
        AtomicInteger toPlace = new AtomicInteger(amount);
        BlockPos.getAllInBox(box).forEach(pos -> {
            if(toPlace.get() > 0 && getManager().getTile().getWorld().getBlockState(pos).getBlock() == Blocks.AIR) {
                getManager().getTile().getWorld().setBlockState(pos, block.getBlockState());
                toPlace.addAndGet(-1);
            }
        });
        return true;
    }

    public boolean replaceBlock(AxisAlignedBB box, PartialBlockState block, int amount, boolean drop, List<IIngredient<PartialBlockState>> filter, boolean whitelist) {
        if(getManager().getTile().getWorld() == null || getBlockAmount(box, filter, whitelist) < amount)
            return false;
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
        box = box.offset(getManager().getTile().getPos());
        AtomicInteger toPlace = new AtomicInteger(amount);
        BlockPos.getAllInBox(box).forEach(pos -> {
            if(toPlace.get() > 0) {
                CachedBlockInfo cached = new CachedBlockInfo(getManager().getTile().getWorld(), pos, false);
                if(filter.stream().flatMap(ingredient -> ingredient.getAll().stream()).anyMatch(state -> state.test(cached)) == whitelist) {
                    if(cached.getBlockState().getMaterial() != Material.AIR)
                        getManager().getTile().getWorld().destroyBlock(pos, drop);
                    getManager().getTile().getWorld().setBlockState(pos, block.getBlockState());
                    toPlace.addAndGet(-1);
                }
            }
        });
        return true;
    }

    public boolean breakBlock(AxisAlignedBB box, List<IIngredient<PartialBlockState>> filter, boolean whitelist, int amount, boolean drop) {
        if(getManager().getTile().getWorld() == null || getBlockAmount(box, filter, whitelist) < amount)
            return false;
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
        box = box.offset(getManager().getTile().getPos());
        AtomicInteger toPlace = new AtomicInteger(amount);
        BlockPos.getAllInBox(box).forEach(pos -> {
            if(toPlace.get() > 0) {
                CachedBlockInfo cached = new CachedBlockInfo(getManager().getTile().getWorld(), pos, false);
                if(filter.stream().flatMap(ingredient -> ingredient.getAll().stream()).anyMatch(state -> state.test(cached)) == whitelist) {
                    if(cached.getBlockState().getMaterial() != Material.AIR)
                        getManager().getTile().getWorld().destroyBlock(pos, drop);
                    toPlace.addAndGet(-1);
                }
            }
        });
        return true;
    }
}
