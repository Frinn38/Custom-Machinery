package fr.frinn.custommachinery.fabric.mixin;

import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.MachineBlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Unique
    private BlockEntity be;

    @ModifyVariable(
            method = "destroyBlock",
            ordinal = 0,
            at = @At("STORE")
    )
    private BlockEntity custommachinery$getBlockEntity(BlockEntity be) {
        this.be = be;
        return be;
    }


    @Redirect(
            method = "destroyBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z")
    )
    private boolean custommachinery$hasCorrectToolForDrops(ServerPlayer player, BlockState state) {
        if(state.getBlock() instanceof CustomMachineBlock && this.be instanceof CustomMachineTile machine && player.hasCorrectToolForDrops(MachineBlockState.CACHE.getUnchecked(machine.getAppearance())))
            return true;
        return player.hasCorrectToolForDrops(state);
    }
}
