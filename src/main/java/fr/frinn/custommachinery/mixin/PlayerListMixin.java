package fr.frinn.custommachinery.mixin;

import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.util.TagIndex;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/network/NetworkHooks;sendMCRegistryPackets(Lnet/minecraft/network/NetworkManager;Ljava/lang/String;)V", shift = At.Shift.AFTER, remap = false))
    private void sendTags(net.minecraft.network.NetworkManager netManager, ServerPlayerEntity playerIn, CallbackInfo ci) {
        NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> playerIn), TagIndex.createUpdatePacket());
    }
}
