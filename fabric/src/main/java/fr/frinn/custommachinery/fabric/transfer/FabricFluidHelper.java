package fr.frinn.custommachinery.fabric.transfer;

import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.util.transfer.IFluidHelper;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class FabricFluidHelper implements IFluidHelper {

    @Override
    public boolean isFluidHandler(ItemStack stack) {
        return FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack)) != null;
    }

    @Override
    public void fillTanksFromStack(List<FluidMachineComponent> tanks, ItemMachineComponent slot) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.ofSingleSlot(new ItemSlot(slot, null)));
        for(FluidMachineComponent component : tanks) {
            FluidTank tank = new FluidTank(component, null);
            StorageUtil.move(storage, tank, f -> true, Long.MAX_VALUE, null);
        }
    }

    @Override
    public void fillStackFromTanks(ItemMachineComponent slot, List<FluidMachineComponent> tanks) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.ofSingleSlot(new ItemSlot(slot, null)));
        for(FluidMachineComponent component : tanks) {
            FluidTank tank = new FluidTank(component, null);
            StorageUtil.move(tank, storage, f -> true, Long.MAX_VALUE, null);
        }
    }

    @Override
    public ItemStack transferFluid(ItemStack stack, FluidMachineComponent component) {
        ContainerItemContext ctx = ContainerItemContext.withConstant(stack);
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ctx);
        FluidTank tank = new FluidTank(component, null);
        long transferred = StorageUtil.move(storage, tank, f -> true, Long.MAX_VALUE, null);
        if(transferred == 0)
            StorageUtil.move(tank, storage, f -> true, Long.MAX_VALUE, null);
        return ctx.getItemVariant().toStack();
    }
}
