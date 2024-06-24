package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class InteractionFluidHandler implements IFluidHandler {

    private final FluidComponentHandler handler;

    public InteractionFluidHandler(FluidComponentHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getTanks() {
        return this.handler.getComponents().size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.handler.getComponents().get(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.handler.getComponents().get(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return this.handler.getComponents().get(tank).isFluidValid(0, stack);
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        AtomicInteger remaining = new AtomicInteger(stack.getAmount());
        this.handler.getComponents().stream()
                .filter(component -> component.isFluidValid(0, stack) && component.getCapacity() - component.getFluid().getAmount() > 0 && component.getMode().isInput())
                .sorted(Comparator.comparingInt(component -> FluidStack.isSameFluidSameComponents(component.getFluid(), stack) ? -1 : 1))
                .forEach(component -> {
                    int toInput = Math.min(remaining.get(), component.fill(stack, FluidAction.SIMULATE));
                    if(toInput > 0) {
                        remaining.addAndGet(-toInput);
                        if (action.execute())
                            component.fill(stack.copyWithAmount(toInput), FluidAction.EXECUTE);
                    }
                });
        return stack.getAmount() - remaining.get();
    }

    @Override
    public FluidStack drain(FluidStack maxDrain, FluidAction action) {
        int remainingToDrain = maxDrain.getAmount();
        for (FluidMachineComponent component : this.handler.getComponents().stream().sorted(Comparator.comparingInt(c -> c.getMode().isOutput() ? -1 : 1)).toList()) {
            if (!component.getFluid().isEmpty() && FluidStack.isSameFluidSameComponents(component.getFluid(), maxDrain)) {
                FluidStack stack = component.drain(maxDrain.getAmount(), FluidAction.SIMULATE);
                if (stack.getAmount() >= remainingToDrain) {
                    if (action.execute())
                        component.drain(maxDrain.getAmount(), FluidAction.EXECUTE);
                    return maxDrain;
                } else {
                    if (action.execute())
                        component.drain(stack.getAmount(), FluidAction.EXECUTE);
                    remainingToDrain -= stack.getAmount();
                }
            }
        }
        if (remainingToDrain == maxDrain.getAmount())
            return FluidStack.EMPTY;
        else
            return maxDrain.copyWithAmount(maxDrain.getAmount() - remainingToDrain);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack toDrain = FluidStack.EMPTY;
        int remainingToDrain = maxDrain;
        for (FluidMachineComponent component : this.handler.getComponents().stream().sorted(Comparator.comparingInt(c -> c.getMode().isOutput() ? -1 : 1)).toList()) {
            if (!component.getFluid().isEmpty() && (toDrain.isEmpty() || FluidStack.isSameFluidSameComponents(component.getFluid(), toDrain))) {
                FluidStack stack = component.drain(remainingToDrain, FluidAction.SIMULATE);
                if (stack.getAmount() >= remainingToDrain) {
                    if (action.execute())
                        component.drain(remainingToDrain, FluidAction.EXECUTE);
                    return stack.copyWithAmount(maxDrain);
                } else {
                    if (toDrain.isEmpty())
                        toDrain = stack;
                    if (action.execute())
                        component.drain(stack.getAmount(), FluidAction.EXECUTE);
                    remainingToDrain -= stack.getAmount();
                }
            }
        }
        if (toDrain.isEmpty() || remainingToDrain == maxDrain)
            return FluidStack.EMPTY;
        else
            return toDrain.copyWithAmount(maxDrain - remainingToDrain);
    }
}
