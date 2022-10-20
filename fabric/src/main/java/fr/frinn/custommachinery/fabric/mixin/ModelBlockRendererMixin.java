package fr.frinn.custommachinery.fabric.mixin;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @Redirect(
            method = "tesselateBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I")
    )
    private int custommachinery$getLightEmission(BlockState state, BlockAndTintGetter level, BakedModel model, BlockState state1, BlockPos pos) {
        if(state.is(Registration.CUSTOM_MACHINE_BLOCK.get()))
            return Registration.CUSTOM_MACHINE_BLOCK.get().getLightEmission(state, level, pos);
        return state.getLightEmission();
    }
}
