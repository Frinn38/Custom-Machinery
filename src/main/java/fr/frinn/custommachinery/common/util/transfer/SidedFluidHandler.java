package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.impl.component.config.IOSideMode;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class SidedFluidHandler implements IFluidHandler {

    private final Direction side;
    private final FluidComponentHandler handler;

    public SidedFluidHandler(Direction side, FluidComponentHandler handler) {
        this.side = side;
        this.handler = handler;
    }

    public List<FluidMachineComponent> getComponentsForMode(Predicate<IOSideMode> filter) {
        return this.handler.getComponents().stream().filter(component -> filter.test(component.getConfig().getSideMode(this.side))).toList();
    }

    @Override
    public int getTanks() {
        return this.handler.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.handler.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.handler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return this.handler.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        FluidStack toFill = resource.copy();
        for(FluidMachineComponent component : this.getComponentsForMode(IOSideMode::isInput)) {
            toFill.shrink(component.fill(toFill, action));
            if(toFill.isEmpty())
                break;
        }
        return resource.getAmount() - toFill.getAmount();
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        int toDrain = 0;
        for(FluidMachineComponent component : this.getComponentsForMode(IOSideMode::isOutput)) {
            toDrain += component.drain(resource.copyWithAmount(resource.getAmount() - toDrain), action).getAmount();
            if(toDrain == resource.getAmount())
                break;
        }
        return resource.copyWithAmount(toDrain);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        for(FluidMachineComponent component : this.getComponentsForMode(IOSideMode::isOutput)) {
            FluidStack drained = component.drain(maxDrain, action);
            if(!drained.isEmpty())
                return drained;
        }
        return FluidStack.EMPTY;
    }
}
