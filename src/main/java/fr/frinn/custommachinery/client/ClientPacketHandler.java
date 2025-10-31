package fr.frinn.custommachinery.client;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.client.integration.jei.CustomMachineryJEIPlugin;
import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineCreationScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.network.SyncableContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ClientPacketHandler {

    public static void handleMachineCoreCountChangePacket(BlockPos pos, int count) {
        if(Minecraft.getInstance().level != null) {
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof CustomMachineTile machineTile && machineTile.getProcessor() instanceof MachineProcessor processor) {
                processor.setClientCoreCount(count);
                if(Minecraft.getInstance().player instanceof Player player && player.containerMenu instanceof CustomMachineContainer container)
                    container.init();
            }
        }
    }

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
        Minecraft mc = Minecraft.getInstance();
        ItemDisplayParameters params = new ItemDisplayParameters(mc.player.connection.enabledFeatures(), mc.player.canUseGameMasterBlocks() && mc.options.operatorItemsTab().get(), mc.level.registryAccess());
        Registration.CUSTOM_MACHINE_TAB.get().buildContents(params);
        if(Minecraft.getInstance().screen instanceof MachineCreationScreen creationScreen)
            creationScreen.reloadList();
        if(ModList.get().isLoaded("jei"))
            CustomMachineryJEIPlugin.reloadMachines(machines);
    }

    public static void handleUpdateTemplatesPacket(Map<ResourceLocation, Pair<CustomMachine, Component>> templates) {
        CustomMachinery.TEMPLATES.clear();
        CustomMachinery.TEMPLATES.putAll(templates);
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

    public static void handleOpenCreationScreenPacket() {
        Minecraft.getInstance().setScreen(new MachineCreationScreen());
    }

    public static void handleOpenEditScreenPacket(ResourceLocation machineId) {
        CustomMachine machine = CustomMachinery.MACHINES.get(machineId);
        if(machine == null)
            machine = CustomMachinery.TEMPLATES.get(machineId).getFirst();
        if(machine != null)
            Minecraft.getInstance().setScreen(new MachineEditScreen(new MachineCreationScreen(), 288, 210, new CustomMachineBuilder(machine)));
    }
}
