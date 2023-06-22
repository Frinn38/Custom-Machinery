package fr.frinn.custommachinery.forge.transfer;

import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.transfer.IFluidHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ForgeFluidHelper implements IFluidHelper {

    @Override
    public boolean isFluidHandler(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }

    @Override
    public void fillTanksFromStack(List<FluidMachineComponent> tanks, ItemMachineComponent slot) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;
        IFluidHandlerItem handlerItem =  stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(() -> new IllegalStateException("Can't fill tanks from non fluid handler item: " + ForgeRegistries.ITEMS.getKey(stack.getItem())));
        for(FluidMachineComponent component : tanks) {
            FluidStack maxExtract;
            if(component.getFluidStack().isEmpty())
                maxExtract = handlerItem.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
            else
                maxExtract = handlerItem.drain(new FluidStack(component.getFluidStack().getFluid(), Integer.MAX_VALUE, component.getFluidStack().getTag()), FluidAction.SIMULATE);

            if(maxExtract.isEmpty())
                continue;

            long maxInsert = component.insert(maxExtract.getFluid(), maxExtract.getAmount(), maxExtract.getTag(), true);

            if(maxInsert <= 0)
                continue;

            FluidStack extracted = handlerItem.drain(new FluidStack(maxExtract.getFluid(), Utils.toInt(maxInsert), maxExtract.getTag()), FluidAction.EXECUTE);

            if(extracted.getAmount() > 0)
                component.insert(extracted.getFluid(), extracted.getAmount(), extracted.getTag(), false);
        }
        slot.setItemStack(handlerItem.getContainer());
    }

    @Override
    public void fillStackFromTanks(ItemMachineComponent slot, List<FluidMachineComponent> tanks) {
        ItemStack stack = slot.getItemStack();
        if(stack.isEmpty())
            return;
        IFluidHandlerItem handlerItem =  stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(() -> new IllegalStateException("Can't fill tanks from non fluid handler item: " + ForgeRegistries.ITEMS.getKey(stack.getItem())));
        for(FluidMachineComponent component : tanks) {
            for(int i = 0; i < handlerItem.getTanks(); i++) {
                if(handlerItem.getFluidInTank(i).isEmpty() || handlerItem.getFluidInTank(i).isFluidEqual(FluidStackHooksForge.toForge(component.getFluidStack()))) {
                    FluidStack maxExtract = FluidStackHooksForge.toForge(component.extract(Integer.MAX_VALUE, true));

                    if(maxExtract.isEmpty())
                        continue;

                    int maxInsert = handlerItem.fill(maxExtract, FluidAction.SIMULATE);

                    if(maxInsert <= 0)
                        continue;

                    FluidStack extracted = FluidStackHooksForge.toForge(component.extract(maxInsert, false));

                    if(extracted.getAmount() > 0)
                        handlerItem.fill(extracted, FluidAction.EXECUTE);
                }
            }
        }
        slot.setItemStack(handlerItem.getContainer());
    }

    @Override
    public ItemStack transferFluid(ItemStack stack, FluidMachineComponent component) {
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
