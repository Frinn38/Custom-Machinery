package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class SidedFluidHandler implements ResourceHandler<FluidResource> {

    @Nullable
    private final Direction side;
    private final FluidComponentHandler handler;

    public SidedFluidHandler(@Nullable Direction side, FluidComponentHandler handler) {
        this.side = side;
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
        FluidMachineComponent component = this.handler.getComponents().get(index);
        if(this.side == null || component.getConfig().getDirectionMode(this.side).isInput())
            component.insert(resource, amount, transaction);
        return 0;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        FluidMachineComponent component = this.handler.getComponents().get(index);
        if(this.side == null || component.getConfig().getDirectionMode(this.side).isOutput())
            component.extract(resource, amount, transaction);
        return 0;
    }
}
