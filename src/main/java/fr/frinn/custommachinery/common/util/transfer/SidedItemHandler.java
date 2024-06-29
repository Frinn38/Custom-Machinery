package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class SidedItemHandler implements IItemHandler {

    private final Direction direction;
    private final ItemComponentHandler handler;

    public SidedItemHandler(Direction direction, ItemComponentHandler handler) {
        this.direction = direction;
        this.handler = handler;
    }

    public ItemComponentHandler getHandler() {
        return this.handler;
    }

    @Override
    public int getSlots() {
        return this.handler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.handler.getComponents().get(slot).getItemStack();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        ItemMachineComponent component = this.handler.getComponents().get(slot);
        if(this.direction != null && !component.getConfig().getSideMode(this.direction).isInput())
            return stack;
        return component.insertItem(0, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemMachineComponent component = this.handler.getComponents().get(slot);
        if((this.direction != null && !component.getConfig().getSideMode(this.direction).isOutput()) || component.getItemStack().isEmpty())
            return ItemStack.EMPTY;
        return component.extractItem(0, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.handler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return this.handler.isItemValid(slot, stack);
    }
}
