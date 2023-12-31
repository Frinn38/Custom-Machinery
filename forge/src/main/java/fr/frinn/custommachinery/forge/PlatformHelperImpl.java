package fr.frinn.custommachinery.forge;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.transfer.*;
import fr.frinn.custommachinery.forge.init.ForgeCustomMachineTile;
import fr.frinn.custommachinery.forge.transfer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.ForgeHooks;

import java.lang.reflect.Field;
import java.util.List;

public class PlatformHelperImpl {

    public static final ForgeEnergyHelper ENERGY_HELPER = new ForgeEnergyHelper();
    public static final ForgeFluidHelper FLUID_HELPER = new ForgeFluidHelper();

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

    public static IEnergyHelper energy() {
        return ENERGY_HELPER;
    }

    public static IFluidHelper fluid() {
        return FLUID_HELPER;
    }

    public static boolean hasCorrectToolsForDrops(Player player, BlockState state) {
        return ForgeHooks.isCorrectToolForDrops(state, player);
    }
}
