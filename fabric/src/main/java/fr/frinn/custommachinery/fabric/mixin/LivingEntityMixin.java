package fr.frinn.custommachinery.fabric.mixin;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {

    @Redirect(method = "playBlockFallSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType custommachinery$getFallSound(BlockState state) {
        if(state.is(Registration.CUSTOM_MACHINE_BLOCK.get())) {
            LivingEntity entity = (LivingEntity) (Object) this;
            BlockPos pos = new BlockPos(Mth.floor(entity.getX()), Mth.floor(entity.getY() - 0.20000000298023224), Mth.floor(entity.getZ()));
            return Registration.CUSTOM_MACHINE_BLOCK.get().getSoundType(state, entity.level, pos, entity);
        }
        return state.getSoundType();
    }
}
