package fr.frinn.custommachinery.fabric.mixin;

import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockGetter.class)
public interface BlockGetterMixin {

    @Redirect(
            method = "getLightEmission",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I")
    )
    private int custommachinery$getLightEmission(BlockState state, BlockPos pos) {
        if(state.getBlock() instanceof CustomMachineBlock machineBlock)
            return machineBlock.getLightEmission(state, (BlockGetter)this, pos);
        return state.getLightEmission();
    }
}