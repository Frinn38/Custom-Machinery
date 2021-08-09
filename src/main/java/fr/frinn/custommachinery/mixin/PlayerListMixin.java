package fr.frinn.custommachinery.mixin;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.network.SUpdateMachinesPacket;
import fr.frinn.custommachinery.common.network.SUpdateUpgradesPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(at = @At("HEAD"), method = "initializeConnectionToPlayer(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/entity/player/ServerPlayerEntity;)V")
    private void sendCustomMachineData(NetworkManager manager, ServerPlayerEntity player, CallbackInfo info) {
        fr.frinn.custommachinery.common.network.NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateMachinesPacket(CustomMachinery.MACHINES));
        fr.frinn.custommachinery.common.network.NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateUpgradesPacket(CustomMachinery.UPGRADES));
    }
}
