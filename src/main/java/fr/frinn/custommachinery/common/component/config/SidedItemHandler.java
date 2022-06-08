package fr.frinn.custommachinery.common.component.config;

import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SidedItemHandler implements IItemHandler {

    @Nullable
    private final Direction direction;
    private final ItemComponentHandler handler;

    public SidedItemHandler(@Nullable Direction direction, ItemComponentHandler handler) {
        this.direction = direction;
        this.handler = handler;
    }

    @Override
    public int getSlots() {
        return this.handler.getComponents().size();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.handler.getComponents().get(slot).getItemStack();
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        ItemMachineComponent component = this.handler.getComponents().get(slot);
        if(this.direction != null && !component.getConfig().getSideMode(this.direction).isInput())
            return stack;
        int maxInsert = component.getSpaceForItem(stack);
        int toInsert = Math.min(maxInsert, stack.getCount());
        if(!simulate) {
            ItemStack stackIn = stack.copy();
            stackIn.setCount(toInsert);
            component.insert(stackIn);
            this.handler.getManager().markDirty();
        }
        ItemStack stackRemaining = stack.copy();
        stackRemaining.shrink(toInsert);
        return stackRemaining;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemMachineComponent component = this.handler.getComponents().get(slot);
        if((this.direction != null && !component.getConfig().getSideMode(this.direction).isOutput()) || component.getItemStack().isEmpty())
            return ItemStack.EMPTY;
        ItemStack stack = component.getItemStack().copy();
        stack.setCount(Math.min(component.getItemStack().getCount(), amount));
        if(!simulate) {
            component.extract(stack.getCount());
            this.handler.getManager().markDirty();
        }
        return stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.handler.getComponents().get(slot).getCapacity();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return this.handler.getComponents().get(slot).isItemValid(stack);
    }
}
