package fr.frinn.custommachinery.forge.transfer;

import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

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
        return FluidStackHooksForge.toForge(this.handler.getComponents().get(tank).getFluidStack());
    }

    @Override
    public int getTankCapacity(int tank) {
        return Utils.toInt(this.handler.getComponents().get(tank).getCapacity());
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return this.handler.getComponents().get(tank).isFluidValid(FluidStackHooksForge.fromForge(stack));
    }

    @Override
    public int fill(FluidStack forgeStack, FluidAction action) {
        AtomicLong remaining = new AtomicLong(forgeStack.getAmount());
        dev.architectury.fluid.FluidStack stack = FluidStackHooksForge.fromForge(forgeStack);
        this.handler.getComponents().stream()
                .filter(component -> component.isFluidValid(stack) && component.getRemainingSpace() > 0 && component.getMode().isInput())
                .sorted(Comparator.comparingInt(component -> component.getFluidStack().isFluidEqual(stack) ? -1 : 1))
                .forEach(component -> {
                    long toInput = Math.min(remaining.get(), component.insert(stack.getFluid(), (int)stack.getAmount(), stack.getTag(), true));
                    if(toInput > 0) {
                        remaining.addAndGet(-toInput);
                        if (action.execute())
                            component.insert(stack.getFluid(), toInput, stack.getTag(), false);
                    }
                });
        return (int) (stack.getAmount() - remaining.get());
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack maxDrain, FluidAction action) {
        long remainingToDrain = maxDrain.getAmount();
        for (FluidMachineComponent component : this.handler.getComponents().stream().sorted(Comparator.comparingInt(c -> c.getMode().isOutput() ? 1 : -1)).toList()) {
            if (!component.getFluidStack().isEmpty() && component.getFluidStack().isFluidEqual(FluidStackHooksForge.fromForge(maxDrain))) {
                FluidStack stack = FluidStackHooksForge.toForge(component.extract(maxDrain.getAmount(), true));
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
            return new FluidStack(maxDrain.getFluid(), (int) (maxDrain.getAmount() - remainingToDrain), maxDrain.getTag());
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack toDrain = FluidStack.EMPTY;
        int remainingToDrain = maxDrain;
        for (FluidMachineComponent component : this.handler.getComponents().stream().sorted(Comparator.comparingInt(c -> c.getMode().isOutput() ? 1 : -1)).toList()) {
            if (!component.getFluidStack().isEmpty() && (toDrain.isEmpty() || component.getFluidStack().isFluidEqual(FluidStackHooksForge.fromForge(toDrain)))) {
                FluidStack stack = FluidStackHooksForge.toForge(component.extract(remainingToDrain, true));
                if (stack.getAmount() >= remainingToDrain) {
                    if (action.execute())
                        component.extract(remainingToDrain, false);
                    return new FluidStack(stack.getFluid(), maxDrain, stack.getTag());
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
            return new FluidStack(toDrain.getFluid(), maxDrain - remainingToDrain, toDrain.getTag());
    }
}
