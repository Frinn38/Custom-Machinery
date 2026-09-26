package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class SidedItemHandler implements ResourceHandler<ItemResource> {

    @Nullable
    private final Direction side;
    private final ItemComponentHandler handler;

    public SidedItemHandler(@Nullable Direction side, ItemComponentHandler handler) {
        this.side = side;
        this.handler = handler;
    }

    public ItemComponentHandler getHandler() {
        return this.handler;
    }

    @Override
    public int size() {
        return this.handler.getComponents().size();
    }

    @Override
    public ItemResource getResource(int index) {
        return this.handler.getComponents().get(index).getResource(0);
    }

    @Override
    public long getAmountAsLong(int index) {
        return this.handler.getComponents().get(index).getAmountAsLong(0);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return this.handler.getComponents().get(index).getCapacityAsLong(0, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return this.handler.getComponents().get(index).isValid(0, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        ItemMachineComponent component = this.handler.getComponents().get(index);
        if(this.side == null || component.getConfig().getDirectionMode(this.side).isInput())
            component.insert(resource, amount, transaction);
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        ItemMachineComponent component = this.handler.getComponents().get(index);
        if(this.side == null || component.getConfig().getDirectionMode(this.side).isOutput())
            component.extract(resource, amount, transaction);
        return 0;
    }
}
