package fr.frinn.custommachinery.forge.transfer;

import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
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
        return this.component.getFluidStack();
    }

    @Override
    public int getTankCapacity(int i) {
        return (int)this.component.getCapacity();
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack stack) {
        return this.component.isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        if(!this.component.isFluidValid(stack))
            return 0;
        return (int)component.insert(stack.getFluid(), stack.getAmount(), null, action.simulate());
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack stack, FluidAction action) {
        if(FluidStack.isSameFluidSameComponents(stack, this.component.getFluidStack()))
            return this.component.extract(stack.getAmount(), action.simulate());
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int amount, FluidAction action) {
        return this.component.extract(amount, action.simulate());
    }
}
