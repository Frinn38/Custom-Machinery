package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.common.data.component.ItemMachineComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotItemComponent extends Slot {

    private static final IInventory EMPTY = new Inventory(0);

    private ItemMachineComponent component;

    public SlotItemComponent(ItemMachineComponent component, int x, int y) {
        super(EMPTY, 0, x, y);
        this.component = component;
    }

    public ItemMachineComponent getComponent() {
        return this.component;
    }

    @Override
    public ItemStack getStack() {
        return this.component.getItemStack();
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return this.component.isItemValid(stack) && this.component.getMode().isInput();
    }

    @Override
    public void putStack(ItemStack stack) {
        this.component.setItemStack(stack);
    }

    @Override
    public int getSlotStackLimit() {
        return this.component.getCapacity();
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        ItemStack stack = this.component.getItemStack().copy();
        this.component.extract(amount);
        stack.setCount(amount);
        return stack;
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        return true;
    }
}
