package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.crafting.craft.CraftProcessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ResultSlotItemComponent extends SlotItemComponent {

    public ResultSlotItemComponent(ItemMachineComponent component, int index, int x, int y) {
        super(component, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if(this.getComponent().getManager().getTile().getProcessor() instanceof CraftProcessor craftProcessor)
            craftProcessor.craft();
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack stack = this.getComponent().getItemStack().copy();
        this.getComponent().setItemStack(ItemStack.EMPTY);
        return stack;
    }
}
