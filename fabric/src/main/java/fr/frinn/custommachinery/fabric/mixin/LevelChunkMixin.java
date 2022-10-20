package fr.frinn.custommachinery.fabric.mixin;

import fr.frinn.custommachinery.common.init.Registration;
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I")
    )
    private int custommachinery$getLightEmission(BlockState state, BlockPos pos) {
        if(state.is(Registration.CUSTOM_MACHINE_BLOCK.get()))
            return Registration.CUSTOM_MACHINE_BLOCK.get().getLightEmission(state, ((LevelChunk) (Object)this).getLevel(), pos);
        return state.getLightEmission();
    }
}
