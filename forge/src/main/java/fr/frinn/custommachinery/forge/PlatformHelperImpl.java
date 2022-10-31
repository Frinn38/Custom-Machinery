package fr.frinn.custommachinery.forge;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.transfer.ICommonEnergyHandler;
import fr.frinn.custommachinery.common.util.transfer.ICommonFluidHandler;
import fr.frinn.custommachinery.common.util.transfer.ICommonItemHandler;
import fr.frinn.custommachinery.forge.init.ForgeCustomMachineBlock;
import fr.frinn.custommachinery.forge.init.ForgeCustomMachineTile;
import fr.frinn.custommachinery.forge.transfer.ForgeEnergyHandler;
import fr.frinn.custommachinery.forge.transfer.ForgeFluidHandler;
import fr.frinn.custommachinery.forge.transfer.ForgeItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.lang.reflect.Field;
import java.util.List;

public class PlatformHelperImpl {

    public static ICommonEnergyHandler createEnergyHandler(EnergyMachineComponent component) {
        return new ForgeEnergyHandler(component);
    }

    public static ICommonFluidHandler createFluidHandler(FluidComponentHandler handler) {
        return new ForgeFluidHandler(handler);
    }

    public static ICommonItemHandler createItemHandler(ItemComponentHandler handler) {
        return new ForgeItemHandler(handler);
    }

    public static CustomMachineTile createMachineTile(BlockPos pos, BlockState state) {
        return new ForgeCustomMachineTile(pos, state);
    }

    public static CustomMachineBlock createMachineBlock() {
        return new ForgeCustomMachineBlock();
    }

    @SuppressWarnings("unchecked")
    public static List<LootPool> getPoolsFromTable(LootTable table) {
        for(Field field : LootTable.class.getDeclaredFields()) {
            if(field.getName().equals("e") || field.getName().equals("f_79109_") || field.getName().equals("pools")) {
                field.setAccessible(true);
                try {
                    return (List<LootPool>) field.get(table);
                } catch (IllegalAccessException ignored) {

                }
            }
        }
        throw new RuntimeException("NOPE");
    }

    public static String energyUnit() {
        return "FE";
    }
}