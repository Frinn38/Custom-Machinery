package fr.frinn.custommachinery.fabric;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.transfer.*;
import fr.frinn.custommachinery.fabric.init.FabricCustomMachineTile;
import fr.frinn.custommachinery.fabric.transfer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Arrays;
import java.util.List;

public class PlatformHelperImpl {

    public static final FabricEnergyHelper ENERGY_HELPER = new FabricEnergyHelper();
    public static final FabricFluidHelper FLUID_HELPER = new FabricFluidHelper();

    public static ICommonEnergyHandler createEnergyHandler(EnergyMachineComponent component) {
        return new FabricEnergyHandler(component);
    }

    public static ICommonFluidHandler createFluidHandler(FluidComponentHandler handler) {
        return new FabricFluidHandler(handler);
    }

    public static ICommonItemHandler createItemHandler(ItemComponentHandler handler) {
        return new FabricItemHandler(handler);
    }

    public static CustomMachineTile createMachineTile(BlockPos pos, BlockState state) {
        return new FabricCustomMachineTile(pos, state);
    }

    public static List<LootPool> getPoolsFromTable(LootTable table) {
        return Arrays.asList(table.pools);
    }

    public static IEnergyHelper energy() {
        return ENERGY_HELPER;
    }

    public static IFluidHelper fluid() {
        return FLUID_HELPER;
    }

    public static boolean hasCorrectToolsForDrops(Player player, BlockState state) {
        return player.hasCorrectToolForDrops(state);
    }
}
