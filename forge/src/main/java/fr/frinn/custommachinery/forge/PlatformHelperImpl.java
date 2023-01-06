package fr.frinn.custommachinery.forge;

import dev.architectury.platform.Platform;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.client.integration.jei.wrapper.FluidIngredientWrapper;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.common.util.transfer.ICommonEnergyHandler;
import fr.frinn.custommachinery.common.util.transfer.ICommonFluidHandler;
import fr.frinn.custommachinery.common.util.transfer.ICommonItemHandler;
import fr.frinn.custommachinery.common.util.transfer.IEnergyHelper;
import fr.frinn.custommachinery.common.util.transfer.IFluidHelper;
import fr.frinn.custommachinery.forge.client.LegacyJei9FluidIngredientWrapper;
import fr.frinn.custommachinery.forge.init.ForgeCustomMachineBlock;
import fr.frinn.custommachinery.forge.init.ForgeCustomMachineTile;
import fr.frinn.custommachinery.forge.transfer.ForgeEnergyHandler;
import fr.frinn.custommachinery.forge.transfer.ForgeEnergyHelper;
import fr.frinn.custommachinery.forge.transfer.ForgeFluidHandler;
import fr.frinn.custommachinery.forge.transfer.ForgeFluidHelper;
import fr.frinn.custommachinery.forge.transfer.ForgeItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

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

    public static IEnergyHelper energy() {
        return ENERGY_HELPER;
    }

    public static IFluidHelper fluid() {
        return FLUID_HELPER;
    }

    public static IJEIIngredientWrapper<?> fluidJeiIngredientWrapper(RequirementIOMode mode, IIngredient<Fluid> fluid, long amount, double chance, boolean isPerTick, CompoundTag nbt, String tank) {
        if(Platform.getMod("jei").getVersion().startsWith("9"))
            return new LegacyJei9FluidIngredientWrapper(mode, fluid, Utils.toInt(amount), chance, isPerTick, nbt, tank);
        return new FluidIngredientWrapper(mode, fluid, amount, chance, isPerTick, nbt, tank);
    }
}
