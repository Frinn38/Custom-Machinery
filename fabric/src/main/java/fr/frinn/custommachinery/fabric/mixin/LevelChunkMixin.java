package fr.frinn.custommachinery.fabric.mixin;

import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {

    @Redirect(
            method = "method_12217",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"),
            require = 0 //Needed for chisels & bits compatibility
    )
    private int custommachinery$getLightEmission(BlockState state, BlockPos pos) {
        if(state.getBlock() instanceof CustomMachineBlock machineBlock)
            return machineBlock.getLightEmission(state, ((LevelChunk) (Object)this).getLevel(), pos);
        return state.getLightEmission();
    }
}
