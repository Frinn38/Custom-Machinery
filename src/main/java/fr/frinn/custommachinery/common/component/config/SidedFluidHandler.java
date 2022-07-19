package fr.frinn.custommachinery.common.component.config;

import fr.frinn.custommachinery.apiimpl.component.config.SideMode;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class SidedFluidHandler implements IFluidHandler {

    @Nullable
    private final Direction direction;
    private final FluidComponentHandler handler;

    public SidedFluidHandler(@Nullable Direction direction, FluidComponentHandler handler) {
        this.direction = direction;
        this.handler = handler;
    }

    public List<FluidMachineComponent> getSideComponents(Predicate<SideMode> filter) {
        if(this.direction == null)
            return this.handler.getComponents();
        return this.handler.getComponents().stream().filter(component -> filter.test(component.getConfig().getSideMode(this.direction))).toList();
    }

    @Override
    public int getTanks() {
        return this.handler.getComponents().size();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.handler.getComponents().get(tank).getFluidStack();
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.handler.getComponents().get(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.handler.getComponents().get(tank).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        AtomicInteger remaining = new AtomicInteger(stack.getAmount());
        this.getSideComponents(SideMode::isInput).stream()
                .filter(component -> component.isFluidValid(stack) && component.getRemainingSpace() > 0 && component.getMode().isInput())
                .sorted(Comparator.comparingInt(component -> component.getFluidStack().isFluidEqual(stack) ? -1 : 1))
                .forEach(component -> {
                    int toInput = Math.min(remaining.get(), component.insert(stack.getFluid(), stack.getAmount(), stack.getTag(), FluidAction.SIMULATE));
                    if(toInput > 0) {
                        remaining.addAndGet(-toInput);
                        if (action.execute()) {
                            component.insert(stack.getFluid(), toInput, stack.getTag(), FluidAction.EXECUTE);
                            this.handler.getManager().markDirty();
                        }
                    }
                });
        return stack.getAmount() - remaining.get();
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack maxDrain, FluidAction action) {
        int remainingToDrain = maxDrain.getAmount();

        for (FluidMachineComponent component : this.getSideComponents(SideMode::isOutput)) {
            if(!component.getFluidStack().isEmpty() && component.getFluidStack().isFluidEqual(maxDrain) && component.getMode().isOutput()) {
                FluidStack stack = component.extract(maxDrain.getAmount(), FluidAction.SIMULATE);
                if(stack.getAmount() >= remainingToDrain) {
                    if(action.execute()) {
                        component.extract(remainingToDrain, FluidAction.EXECUTE);
                        this.handler.getManager().markDirty();
                    }
                    return maxDrain;
                } else {
                    if(action.execute()) {
                        component.extract(stack.getAmount(), FluidAction.EXECUTE);
                        this.handler.getManager().markDirty();
                    }
                    remainingToDrain -= stack.getAmount();
                }
            }
        }
        if(remainingToDrain == maxDrain.getAmount())
            return FluidStack.EMPTY;
        else
            return new FluidStack(maxDrain.getFluid(), maxDrain.getAmount() - remainingToDrain, maxDrain.getTag());
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack toDrain = FluidStack.EMPTY;
        int remainingToDrain = maxDrain;
        for (FluidMachineComponent component : this.getSideComponents(SideMode::isOutput)) {
            if(!component.getFluidStack().isEmpty() && component.getMode().isOutput() && (toDrain.isEmpty() || component.getFluidStack().isFluidEqual(toDrain))) {
                FluidStack stack = component.extract(remainingToDrain, FluidAction.SIMULATE);
                if(stack.getAmount() >= remainingToDrain) {
                    if(action.execute()) {
                        component.extract(remainingToDrain, FluidAction.EXECUTE);
                        this.handler.getManager().markDirty();
                    }
                    return new FluidStack(stack.getFluid(), maxDrain, stack.getTag());
                } else {
                    if(toDrain.isEmpty())
                        toDrain = stack;
                    if(action.execute()) {
                        component.extract(stack.getAmount(), FluidAction.EXECUTE);
                        this.handler.getManager().markDirty();
                    }
                    remainingToDrain -= stack.getAmount();
                }
            }
        }
        if(toDrain.isEmpty() || remainingToDrain == maxDrain)
            return FluidStack.EMPTY;
        else
            return new FluidStack(toDrain.getFluid(), maxDrain - remainingToDrain, toDrain.getTag());
    }
}
