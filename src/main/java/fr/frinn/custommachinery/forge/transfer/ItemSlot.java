package fr.frinn.custommachinery.forge.transfer;

import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemSlot implements IItemHandler {

    private final ItemMachineComponent component;
    @Nullable
    private final Direction side;

    public ItemSlot(ItemMachineComponent component, @Nullable Direction side) {
        this.component = component;
        this.side = side;
    }

    public ItemMachineComponent getComponent() {
        return this.component;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int index) {
        return this.component.getItemStack();
    }

    @NotNull
    @Override
    public ItemStack insertItem(int index, @NotNull ItemStack stack, boolean simulate) {
        if(this.side != null && !this.component.getConfig().getSideMode(this.side).isInput())
            return stack;

        if(!this.component.isItemValid(stack))
            return stack;

        int inserted = this.component.insert(stack.getItem(), stack.getCount(), null, simulate);
        if(inserted == 0)
            return stack;
        else if(inserted == stack.getCount())
            return ItemStack.EMPTY;
        else
            return Utils.makeItemStack(stack.getItem(), stack.getCount() - inserted, null);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int index, int amount, boolean simulate) {
        if(this.side != null && !this.component.getConfig().getSideMode(this.side).isOutput())
            return ItemStack.EMPTY;

        return this.component.extract(amount, simulate);
    }

    @Override
    public int getSlotLimit(int index) {
        return this.component.getCapacity();
    }

    @Override
    public boolean isItemValid(int index, @NotNull ItemStack stack) {
        return this.component.isItemValid(stack);
    }
}
