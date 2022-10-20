package fr.frinn.custommachinery.forge.init;

import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.MachineBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ForgeCustomMachineBlock extends CustomMachineBlock {

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return Optional.ofNullable(level.getBlockEntity(pos))
                .filter(blockEntity -> blockEntity instanceof CustomMachineTile)
                .map(blockEntity -> (CustomMachineTile)blockEntity)
                .map(machine -> ForgeHooks.isCorrectToolForDrops(MachineBlockState.CACHE.getUnchecked(machine.getMachine().getAppearance(machine.getStatus())), player))
                .orElse(super.canHarvestBlock(state, level, pos, player));
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return super.getSoundType(state, level, pos, entity);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return super.getLightEmission(state, level, pos);
    }
}
