package fr.frinn.custommachinery.mixin;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.network.SUpdateMachinesPacket;
import fr.frinn.custommachinery.common.network.SUpdateUpgradesPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkHooks.class)
public class NetworkHooksMixin {

    @Inject(at = @At("HEAD"), method = "syncCustomTagTypes(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/tags/ITagCollectionSupplier;)V", remap = false)
    private static void sendCustomMachineData(ServerPlayerEntity player, ITagCollectionSupplier supplier, CallbackInfo info) {
        fr.frinn.custommachinery.common.network.NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateMachinesPacket(CustomMachinery.MACHINES));
        fr.frinn.custommachinery.common.network.NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateUpgradesPacket(CustomMachinery.UPGRADES));
    }
}
