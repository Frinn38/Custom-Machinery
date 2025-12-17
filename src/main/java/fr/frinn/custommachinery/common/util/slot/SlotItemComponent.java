package fr.frinn.custommachinery.common.util.slot;

import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotItemComponent extends Slot {

    private static final Container EMPTY = new SimpleContainer(0);

    private final ItemMachineComponent component;

    public SlotItemComponent(ItemMachineComponent component, int index, int x, int y) {
        super(EMPTY, index, x, y);
        this.component = component;
    }

    public ItemMachineComponent getComponent() {
        return this.component;
    }

    @Override
    public ItemStack getItem() {
        return this.component.getItemStack();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if(this.component.isLocked())
            return false;
        if(this.component.getType() == Registration.ITEM_MACHINE_COMPONENT.get())
            return this.component.getMode().isInput() && this.component.isItemValid(0, stack);
        return this.component.isItemValid(0, stack);
    }

    @Override
    public void set(ItemStack stack) {
        this.component.setItemStack(stack);
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int increment) {
        if(!stack.isEmpty() && this.mayPlace(stack)) {
            ItemStack itemstack = this.getItem();
            int i = Math.min(Math.min(increment, stack.getCount()), this.getMaxStackSize(stack) - itemstack.getCount());
            if(itemstack.isEmpty()) {
                this.setByPlayer(stack.split(i));
            } else if(ItemStack.isSameItemSameComponents(itemstack, stack)) {
                stack.shrink(i);
                //itemstack.grow(i); DO NOT MODIFY THE STORED STACK DIRECTLY
                //this.setByPlayer(itemstack); Instead set a modified copy of the stack so upgrades are refreshed.
                this.setByPlayer(itemstack.copyWithCount(itemstack.getCount() + i));
            }
        }
        return stack;
    }

    @Override
    public int getMaxStackSize() {
        return this.component.getCapacity();
    }

    @Override
    public ItemStack remove(int amount) {
        return this.component.extractItemBypassLimit(amount, false);
    }

    @Override
    public boolean mayPickup(Player player) {
        return !this.component.isLocked();
    }
}
