package fr.frinn.custommachinery.fabric.mixin;

import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityMixin {

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void custommachinery$playStepSound(BlockPos pos, BlockState state, CallbackInfo ci) {
        if(state.getBlock() instanceof CustomMachineBlock machineBlock) {
            Entity entity = (Entity) (Object) this;
            SoundType sound = machineBlock.getSoundType(state, entity.level, pos, entity);
            entity.playSound(sound.getStepSound(), sound.getVolume() * 0.15F, sound.getPitch());
            ci.cancel();
        }
    }
}
