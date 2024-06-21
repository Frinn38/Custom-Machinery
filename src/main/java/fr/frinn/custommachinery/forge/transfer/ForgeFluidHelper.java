package fr.frinn.custommachinery.forge.transfer;

import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.List;

public class ForgeFluidHelper {

    public static boolean isFluidHandler(ItemStack stack) {
        return stack.getCapability(FluidHandler.ITEM) != null;
    }

    public static void fillTanksFromStack(List<FluidMachineComponent> tanks, ItemMachineComponent slot) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;

        IFluidHandlerItem handlerItem =  stack.getCapability(FluidHandler.ITEM);
        if(handlerItem == null)
            return;

        for(FluidMachineComponent component : tanks) {
            FluidStack maxExtract;
            if(component.getFluidStack().isEmpty())
                maxExtract = handlerItem.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
            else
                maxExtract = handlerItem.drain(new FluidStack(component.getFluidStack().getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE);

            if(maxExtract.isEmpty())
                continue;

            long maxInsert = component.insert(maxExtract.getFluid(), maxExtract.getAmount(), null, true);

            if(maxInsert <= 0)
                continue;

            FluidStack extracted = handlerItem.drain(new FluidStack(maxExtract.getFluid(), Utils.toInt(maxInsert)), FluidAction.EXECUTE);

            if(extracted.getAmount() > 0)
                component.insert(extracted.getFluid(), extracted.getAmount(), null, false);
        }
        slot.setItemStack(handlerItem.getContainer());
    }

    public static void fillStackFromTanks(ItemMachineComponent slot, List<FluidMachineComponent> tanks) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;

        IFluidHandlerItem handlerItem = stack.getCapability(FluidHandler.ITEM);
        if(handlerItem == null)
            return;

        for(FluidMachineComponent component : tanks) {
            for(int i = 0; i < handlerItem.getTanks(); i++) {
                if(handlerItem.getFluidInTank(i).isEmpty() || FluidStack.isSameFluidSameComponents(handlerItem.getFluidInTank(i), component.getFluidStack())) {
                    FluidStack maxExtract = component.extract(Integer.MAX_VALUE, true);

                    if(maxExtract.isEmpty())
                        continue;

                    int maxInsert = handlerItem.fill(maxExtract, FluidAction.SIMULATE);

                    if(maxInsert <= 0)
                        continue;

                    FluidStack extracted = component.extract(maxInsert, false);

                    if(extracted.getAmount() > 0)
                        handlerItem.fill(extracted, FluidAction.EXECUTE);
                }
            }
        }
        slot.setItemStack(handlerItem.getContainer());
    }

    public static ItemStack transferFluid(ItemStack stack, FluidMachineComponent component) {
        FluidTank tank = new FluidTank(component);
        FluidActionResult result = FluidUtil.tryEmptyContainer(stack, tank, Integer.MAX_VALUE, null, true);
        if(result.isSuccess())
            return result.getResult();
        result = FluidUtil.tryFillContainer(stack, tank, Integer.MAX_VALUE, null, true);
        if(result.isSuccess())
            return result.getResult();
        return stack;
    }
}
