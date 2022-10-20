package fr.frinn.custommachinery.fabric.mixin;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {

    @Redirect(
            method = "levelEvent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;")
    )
    private SoundType custommachinery$getBrokenSoundType(BlockState state, Player player, int i, BlockPos blockPos, int j) {
        if(i == 2001 && state.is(Registration.CUSTOM_MACHINE_BLOCK.get()))
            return Registration.CUSTOM_MACHINE_BLOCK.get().getSoundType(state, player.level, blockPos, null);
        return state.getSoundType();
    }

    @Redirect(
            method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I")
    )
    private static int custommachinery$getLightEmission(BlockState state, BlockAndTintGetter level, BlockState state1, BlockPos pos) {
        if(state.is(Registration.CUSTOM_MACHINE_BLOCK.get()))
            return Registration.CUSTOM_MACHINE_BLOCK.get().getLightEmission(state, level, pos);
        return state.getLightEmission();
    }
}
