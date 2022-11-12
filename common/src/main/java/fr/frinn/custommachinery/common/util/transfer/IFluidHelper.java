package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IFluidHelper {

    boolean isFluidHandler(ItemStack stack);

    void fillTanksFromStack(List<FluidMachineComponent> tanks, ItemMachineComponent component);

    void fillStackFromTanks(ItemMachineComponent stack, List<FluidMachineComponent> tanks);
}
