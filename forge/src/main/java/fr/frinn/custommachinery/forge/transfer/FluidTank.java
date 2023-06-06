package fr.frinn.custommachinery.forge.transfer;

import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class FluidTank implements IFluidHandler {

    private final FluidMachineComponent component;

    public FluidTank(FluidMachineComponent component) {
        this.component = component;
    }


    @Override
    public int getTanks() {
        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int i) {
        return FluidStackHooksForge.toForge(this.component.getFluidStack());
    }

    @Override
    public int getTankCapacity(int i) {
        return (int)this.component.getCapacity();
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack stack) {
        return this.component.isFluidValid(FluidStackHooksForge.fromForge(stack));
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        if(!this.component.isFluidValid(FluidStackHooksForge.fromForge(stack)))
            return 0;
        return (int)component.insert(stack.getFluid(), stack.getAmount(), stack.getTag(), action.simulate());
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack stack, FluidAction action) {
        if(stack.isFluidEqual(FluidStackHooksForge.toForge(this.component.getFluidStack())))
            return FluidStackHooksForge.toForge(this.component.extract(stack.getAmount(), action.simulate()));
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int amount, FluidAction action) {
        return FluidStackHooksForge.toForge(this.component.extract(amount, action.simulate()));
    }
}
