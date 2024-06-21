package fr.frinn.custommachinery.forge.transfer;

import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class SidedFluidStorage implements IFluidHandler {

    @Nullable
    private final Direction direction;
    private final FluidComponentHandler handler;

    public SidedFluidStorage(@Nullable Direction direction, FluidComponentHandler handler) {
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
        return Utils.toInt(this.handler.getComponents().get(tank).getCapacity());
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.handler.getComponents().get(tank).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        AtomicLong remaining = new AtomicLong(stack.getAmount());
        this.getSideComponents(SideMode::isInput).stream()
                .filter(component -> component.isFluidValid(stack) && component.getRemainingSpace() > 0 && component.getMode().isInput())
                .sorted(Comparator.comparingInt(component -> FluidStack.isSameFluidSameComponents(component.getFluidStack(), stack) ? -1 : 1))
                .forEach(component -> {
                    long toInput = Math.min(remaining.get(), component.insert(stack.getFluid(), stack.getAmount(), null, true));
                    if(toInput > 0) {
                        remaining.addAndGet(-toInput);
                        if (action.execute())
                            component.insert(stack.getFluid(), toInput, null, false);
                    }
                });
        return (int) (stack.getAmount() - remaining.get());
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack maxDrain, FluidAction action) {
        int remainingToDrain = maxDrain.getAmount();

        for (FluidMachineComponent component : this.getSideComponents(SideMode::isOutput)) {
            if(!component.getFluidStack().isEmpty() && FluidStack.isSameFluidSameComponents(component.getFluidStack(), maxDrain)) {
                FluidStack stack = component.extract(maxDrain.getAmount(), true);
                if(stack.getAmount() >= remainingToDrain) {
                    if(action.execute())
                        component.extract(remainingToDrain, false);
                    return maxDrain;
                } else {
                    if(action.execute())
                        component.extract(stack.getAmount(), false);
                    remainingToDrain -= stack.getAmount();
                }
            }
        }
        if(remainingToDrain == maxDrain.getAmount())
            return FluidStack.EMPTY;
        else
            return new FluidStack(maxDrain.getFluid(), maxDrain.getAmount() - remainingToDrain);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack toDrain = FluidStack.EMPTY;
        int remainingToDrain = maxDrain;
        for (FluidMachineComponent component : this.getSideComponents(SideMode::isOutput)) {
            if(!component.getFluidStack().isEmpty() && (toDrain.isEmpty() || FluidStack.isSameFluidSameComponents(component.getFluidStack(), toDrain))) {
                FluidStack stack = component.extract(remainingToDrain, true);
                if(stack.getAmount() >= remainingToDrain) {
                    if(action.execute())
                        component.extract(remainingToDrain, false);
                    return new FluidStack(stack.getFluid(), maxDrain);
                } else {
                    if(toDrain.isEmpty())
                        toDrain = stack;
                    if(action.execute())
                        component.extract(stack.getAmount(), false);
                    remainingToDrain -= stack.getAmount();
                }
            }
        }
        if(toDrain.isEmpty() || remainingToDrain == maxDrain)
            return FluidStack.EMPTY;
        else
            return new FluidStack(toDrain.getFluid(), maxDrain - remainingToDrain);
    }
}
