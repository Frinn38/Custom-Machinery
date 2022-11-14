package fr.frinn.custommachinery.fabric.init;

import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.MachineBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FabricCustomMachineBlock extends CustomMachineBlock {

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        if(level.getBlockEntity(pos) instanceof CustomMachineTile machine) {
            if(player.hasCorrectToolForDrops(MachineBlockState.CACHE.getUnchecked(machine.getMachine().getAppearance(machine.getStatus()))))
                super.playerDestroy(level, player, pos, state, machine, player.getUseItem());
        }
    }
}
