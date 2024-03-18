package fr.frinn.custommachinery.client;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.creator.MachineCreationScreen;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.network.SyncableContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ClientPacketHandler {

    public static void handleMachineStatusChangedPacket(BlockPos pos, MachineStatus status) {
        if(Minecraft.getInstance().level != null) {
            BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile machineTile && status != machineTile.getStatus()) {
                machineTile.setStatus(status);
                machineTile.refreshClientData();
                Minecraft.getInstance().level.sendBlockUpdated(pos, tile.getBlockState(), tile.getBlockState(), Block.UPDATE_ALL);
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

    public static void handleUpdateMachinesPacket(Map<ResourceLocation, CustomMachine> machines) {
        CustomMachinery.MACHINES.clear();
        CustomMachinery.MACHINES.putAll(machines);
        Registration.GROUP.fillItemList(NonNullList.create());
        MachineCreationScreen.INSTANCE.refreshMachineList();
    }

    public static void handleUpdateMachineAppearancePacket(BlockPos pos, @Nullable MachineAppearance appearance) {
        if(Minecraft.getInstance().level != null) {
            BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile machineTile) {
                machineTile.setCustomAppearance(appearance);
                machineTile.refreshClientData();
                Minecraft.getInstance().level.sendBlockUpdated(pos, machineTile.getBlockState(), machineTile.getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public static void handleUpdateMachineGuiElementsPacket(BlockPos pos, List<IGuiElement> elements) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null) {
            BlockEntity tile = mc.level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile machineTile) {
                machineTile.setCustomGuiElements(elements);
                if(mc.screen instanceof CustomMachineScreen screen) {
                    screen.resize(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                    screen.getMenu().init();
                }
            }
        }
    }
}
