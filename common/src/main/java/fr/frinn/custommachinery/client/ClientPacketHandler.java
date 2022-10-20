package fr.frinn.custommachinery.client;

import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.network.SyncableContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class ClientPacketHandler {

    public static void handleCraftingManagerStatusChangedPacket(BlockPos pos, MachineStatus status) {
        if(Minecraft.getInstance().level != null) {
            BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile machineTile) {
                CraftingManager manager = machineTile.craftingManager;
                if(status != manager.getStatus()) {
                    manager.setStatus(status);
                    machineTile.refreshClientData();
                    Minecraft.getInstance().level.sendBlockUpdated(pos, tile.getBlockState(), tile.getBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    public static void handleRefreshCustomMachineTilePacket(BlockPos pos, ResourceLocation machine) {
        if(Minecraft.getInstance().level != null) {
            BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile machineTile) {
                machineTile.setId(machine);
                machineTile.refreshClientData();
                Minecraft.getInstance().level.sendBlockUpdated(pos, machineTile.getBlockState(), machineTile.getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public static void handleUpdateContainerPacket(int windowId, List<IData<?>> data) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null && player.containerMenu instanceof SyncableContainer container && player.containerMenu.containerId == windowId) {
            data.forEach(container::handleData);
        }
    }
}
