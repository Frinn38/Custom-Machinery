package fr.frinn.custommachinery.fabric.init;

import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FabricCustomMachineTile extends CustomMachineTile {

    public FabricCustomMachineTile(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void refreshClientData() {

    }
}
