package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class InteractionFluidHandler implements ResourceHandler<FluidResource> {

    private final FluidComponentHandler handler;

    public InteractionFluidHandler(FluidComponentHandler handler) {
        this.handler = handler;
    }

    @Override
    public int size() {
        return this.handler.getComponents().size();
    }

    @Override
    public FluidResource getResource(int index) {
        return this.handler.getComponents().get(index).getResource(0);
    }

    @Override
    public long getAmountAsLong(int index) {
        return this.handler.getComponents().get(index).getAmountAsLong(0);
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return this.handler.getComponents().get(index).getCapacityAsLong(0, resource);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return this.handler.getComponents().get(index).isValid(0, resource);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        AtomicInteger remainingToInsert = new AtomicInteger(amount);
        this.handler.getComponents().stream()
                .filter(component -> component.isValid(0, resource) && component.getCapacity() - component.getFluid().getAmount() > 0 && component.getMode().isInput())
                .sorted(Comparator.comparingInt(component -> resource.matches(component.getFluid()) ? -1 : 1))
                .forEach(component -> remainingToInsert.addAndGet(-component.insert(0, resource, remainingToInsert.get(), transaction)));
        return amount - remainingToInsert.get();
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        int remainingToDrain = amount;
        for (FluidMachineComponent component : this.handler.getComponents().stream().sorted(Comparator.comparingInt(c -> c.getMode().isOutput() ? -1 : 1)).toList())
            if (!component.getFluid().isEmpty() && resource.matches(component.getFluid()))
                remainingToDrain -= component.extract(0, resource, remainingToDrain, transaction);
        return amount - remainingToDrain;
    }
}
