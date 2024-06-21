package fr.frinn.custommachinery.forge.transfer;

import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.util.Utils;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

class InteractionFluidStorage implements IFluidHandler {

    private final FluidComponentHandler handler;

    public InteractionFluidStorage(FluidComponentHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getTanks() {
        return this.handler.getComponents().size();
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.handler.getComponents().get(tank).getFluidStack();
    }

    @Override
    public int getTankCapacity(int tank) {
        return Utils.toInt(this.handler.getComponents().get(tank).getCapacity());
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return this.handler.getComponents().get(tank).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        AtomicLong remaining = new AtomicLong(stack.getAmount());
        this.handler.getComponents().stream()
                .filter(component -> component.isFluidValid(stack) && component.getRemainingSpace() > 0 && component.getMode().isInput())
                .sorted(Comparator.comparingInt(component -> FluidStack.isSameFluidSameComponents(component.getFluidStack(), stack) ? -1 : 1))
                .forEach(component -> {
                    long toInput = Math.min(remaining.get(), component.insert(stack.getFluid(), (int)stack.getAmount(), null, true));
                    if(toInput > 0) {
                        remaining.addAndGet(-toInput);
                        if (action.execute())
                            component.insert(stack.getFluid(), toInput, null, false);
                    }
                });
        return (int) (stack.getAmount() - remaining.get());
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack maxDrain, FluidAction action) {
        long remainingToDrain = maxDrain.getAmount();
        for (FluidMachineComponent component : this.handler.getComponents().stream().sorted(Comparator.comparingInt(c -> c.getMode().isOutput() ? 1 : -1)).toList()) {
            if (!component.getFluidStack().isEmpty() && FluidStack.isSameFluidSameComponents(component.getFluidStack(), maxDrain)) {
                FluidStack stack = component.extract(maxDrain.getAmount(), true);
                if (stack.getAmount() >= remainingToDrain) {
                    if (action.execute())
                        component.extract(maxDrain.getAmount(), false);
                    return maxDrain;
                } else {
                    if (action.execute())
                        component.extract(stack.getAmount(), false);
                    remainingToDrain -= stack.getAmount();
                }
            }
        }
        if (remainingToDrain == maxDrain.getAmount())
            return FluidStack.EMPTY;
        else
            return new FluidStack(maxDrain.getFluid(), (int) (maxDrain.getAmount() - remainingToDrain));
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack toDrain = FluidStack.EMPTY;
        int remainingToDrain = maxDrain;
        for (FluidMachineComponent component : this.handler.getComponents().stream().sorted(Comparator.comparingInt(c -> c.getMode().isOutput() ? 1 : -1)).toList()) {
            if (!component.getFluidStack().isEmpty() && (toDrain.isEmpty() || FluidStack.isSameFluidSameComponents(component.getFluidStack(), toDrain))) {
                FluidStack stack = component.extract(remainingToDrain, true);
                if (stack.getAmount() >= remainingToDrain) {
                    if (action.execute())
                        component.extract(remainingToDrain, false);
                    return new FluidStack(stack.getFluid(), maxDrain);
                } else {
                    if (toDrain.isEmpty())
                        toDrain = stack;
                    if (action.execute())
                        component.extract(stack.getAmount(), false);
                    remainingToDrain -= stack.getAmount();
                }
            }
        }
        if (toDrain.isEmpty() || remainingToDrain == maxDrain)
            return FluidStack.EMPTY;
        else
            return new FluidStack(toDrain.getFluid(), maxDrain - remainingToDrain);
    }
}
