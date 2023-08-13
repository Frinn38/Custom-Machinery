package fr.frinn.custommachinery.fabric;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import fr.frinn.custommachinery.fabric.client.ClientHandler;
import fr.frinn.custommachinery.fabric.integration.jade.CMWailaPlugin;
import fr.frinn.custommachinery.fabric.transfer.FabricEnergyHandler;
import fr.frinn.custommachinery.fabric.transfer.FabricFluidHandler;
import fr.frinn.custommachinery.fabric.transfer.FabricItemHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import team.reborn.energy.api.EnergyStorage;

public class CustomMachineryFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CustomMachinery.init();

        if(Platform.isModLoaded("jade"))
            CMWailaPlugin.addMachineBlockToPickedResults();

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(this::afterDatapackReload);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(this::syncDatapacks);

        EnvExecutor.runInEnv(Env.CLIENT, () -> ClientHandler::init);

        LifecycleEvent.SETUP.register(this::createHandlers);
    }

    private void afterDatapackReload(MinecraftServer server, ResourceManager manager, boolean success) {
        if(success)
            LootTableHelper.generate(server);
    }

    private void syncDatapacks(ServerPlayer player, boolean joined) {
        CustomMachinery.syncData(player);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void createHandlers() {
        EnergyStorage.SIDED.registerForBlockEntity(
                (machine, side) -> machine.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get())
                        .map(component -> ((FabricEnergyHandler)component.getEnergyHandler()).getStorage(side))
                        .orElse(null),
                Registration.CUSTOM_MACHINE_TILE.get()
        );
        FluidStorage.SIDED.registerForBlockEntity(
                (machine, side) -> machine.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                        .map(handler -> ((FabricFluidHandler)((FluidComponentHandler)handler).getCommonFluidHandler()).getFluidStorage(side))
                        .orElse(null),
                Registration.CUSTOM_MACHINE_TILE.get()
        );
        ItemStorage.SIDED.registerForBlockEntity(
                (machine, side) -> machine.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                        .map(handler -> ((FabricItemHandler)((ItemComponentHandler)handler).getCommonHandler()).getItemStorage(side))
                        .orElse(null),
                Registration.CUSTOM_MACHINE_TILE.get()
        );
    }
}
