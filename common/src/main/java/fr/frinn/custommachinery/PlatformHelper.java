package fr.frinn.custommachinery;

import dev.architectury.injectables.annotations.ExpectPlatform;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.transfer.ICommonEnergyHandler;
import fr.frinn.custommachinery.common.util.transfer.ICommonFluidHandler;
import fr.frinn.custommachinery.common.util.transfer.ICommonItemHandler;
import fr.frinn.custommachinery.common.util.transfer.IEnergyHelper;
import fr.frinn.custommachinery.common.util.transfer.IFluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public class PlatformHelper {

    @ExpectPlatform
    public static ICommonEnergyHandler createEnergyHandler(EnergyMachineComponent component) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ICommonFluidHandler createFluidHandler(FluidComponentHandler handler) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ICommonItemHandler createItemHandler(ItemComponentHandler handler) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CustomMachineTile createMachineTile(BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static List<LootPool> getPoolsFromTable(LootTable table) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static IEnergyHelper energy() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static IFluidHelper fluid() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean hasCorrectToolsForDrops(Player player, BlockState state) {
        throw new AssertionError();
    }
}
