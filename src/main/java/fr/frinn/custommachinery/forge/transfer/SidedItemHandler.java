package fr.frinn.custommachinery.forge.transfer;

import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SidedItemHandler implements IItemHandler {

    @Nullable
    private final Direction direction;
    private final ItemComponentHandler handler;
    private final List<ItemSlot> slots;

    public SidedItemHandler(@Nullable Direction direction, ItemComponentHandler handler) {
        this.direction = direction;
        this.handler = handler;
        this.slots = handler.getComponents().stream().map(component -> new ItemSlot(component, direction)).toList();
    }

    public List<ItemSlot> getSlotList() {
        return this.slots;
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
        int maxInsert = component.insert(stack.getItem(), stack.getCount(), null, true);
        if(!simulate) {
            component.insert(stack.getItem(), maxInsert, null, false);
            this.handler.getManager().markDirty();
        }
        ItemStack stackRemaining = stack.copy();
        stackRemaining.shrink(maxInsert);
        return stackRemaining;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemMachineComponent component = this.handler.getComponents().get(slot);
        if((this.direction != null && !component.getConfig().getSideMode(this.direction).isOutput()) || component.getItemStack().isEmpty())
            return ItemStack.EMPTY;
        ItemStack stack = component.extract(amount, true);
        if(!stack.isEmpty() && !simulate) {
            component.extract(stack.getCount(), false);
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
