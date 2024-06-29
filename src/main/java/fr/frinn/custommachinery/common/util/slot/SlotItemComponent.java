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

    @Override
    public void setChanged() {
        this.component.getManager().markDirty();
    }
}
