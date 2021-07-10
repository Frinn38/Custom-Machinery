package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.block.Blocks;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.atomic.AtomicInteger;

public class BlockMachineComponent extends AbstractMachineComponent {

    public BlockMachineComponent(MachineComponentManager manager) {
        super(manager, Mode.BOTH);
    }

    @Override
    public MachineComponentType<BlockMachineComponent> getType() {
        return Registration.BLOCK_MACHINE_COMPONENT.get();
    }

    public long getBlockAmount(AxisAlignedBB box, PartialBlockState block) {
        if(getManager().getTile().getWorld() == null)
            return 0;
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
        box = box.offset(getManager().getTile().getPos());
        return BlockPos.getAllInBox(box).map(pos -> new CachedBlockInfo(getManager().getTile().getWorld(), pos, false)).filter(block).count();
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

    public boolean replaceBlock(AxisAlignedBB box, PartialBlockState block, int amount, boolean drop) {
        if(getManager().getTile().getWorld() == null)
            return false;
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
        box = box.offset(getManager().getTile().getPos());
        if(BlockPos.getAllInBox(box).map(getManager().getTile().getWorld()::getBlockState).count() < amount)
            return false;
        AtomicInteger toPlace = new AtomicInteger(amount);
        BlockPos.getAllInBox(box).forEach(pos -> {
            if(toPlace.get() > 0) {
                if(getManager().getTile().getWorld().getBlockState(pos).getBlock() != Blocks.AIR)
                    getManager().getTile().getWorld().destroyBlock(pos, drop);
                getManager().getTile().getWorld().setBlockState(pos, block.getBlockState());
                toPlace.addAndGet(-1);
            }
        });
        return true;
    }

    public boolean breakBlock(AxisAlignedBB box, PartialBlockState block, int amount, boolean drop) {
        if(getManager().getTile().getWorld() == null)
            return false;
        box = Utils.rotateBox(box, getManager().getTile().getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
        box = box.offset(getManager().getTile().getPos());
        if(BlockPos.getAllInBox(box).map(pos -> new CachedBlockInfo(getManager().getTile().getWorld(), pos, false)).filter(block).count() < amount)
            return false;
        AtomicInteger toPlace = new AtomicInteger(amount);
        BlockPos.getAllInBox(box).forEach(pos -> {
            if(toPlace.get() > 0 && block.test(new CachedBlockInfo(getManager().getTile().getWorld(), pos, false))) {
                getManager().getTile().getWorld().destroyBlock(pos, drop);
                toPlace.addAndGet(-1);
            }
        });
        return true;
    }
}
