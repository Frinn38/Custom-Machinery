package fr.frinn.custommachinery.fabric;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.transfer.ICommonEnergyHandler;
import fr.frinn.custommachinery.common.util.transfer.ICommonFluidHandler;
import fr.frinn.custommachinery.common.util.transfer.ICommonItemHandler;
import fr.frinn.custommachinery.common.util.transfer.IFluidHelper;
import fr.frinn.custommachinery.fabric.init.FabricCustomMachineBlock;
import fr.frinn.custommachinery.fabric.init.FabricCustomMachineTile;
import fr.frinn.custommachinery.fabric.transfer.FabricEnergyHandler;
import fr.frinn.custommachinery.fabric.transfer.FabricFluidHandler;
import fr.frinn.custommachinery.fabric.transfer.FabricFluidHelper;
import fr.frinn.custommachinery.fabric.transfer.FabricItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Arrays;
import java.util.List;

public class PlatformHelperImpl {

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

    public static CustomMachineBlock createMachineBlock() {
        return new FabricCustomMachineBlock();
    }

    public static List<LootPool> getPoolsFromTable(LootTable table) {
        return Arrays.asList(table.pools);
    }

    public static String energyUnit() {
        return "E";
    }

    public static IFluidHelper fluid() {
        return FLUID_HELPER;
    }
}
