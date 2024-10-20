package fr.frinn.custommachinery.common.util.slot;

import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.network.CSetFilterSlotItemPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class FilterSlotItemComponent extends SlotItemComponent {
    public FilterSlotItemComponent(ItemMachineComponent component, int index, int x, int y) {
        super(component, index, x, y);
    }

    //Call from the client only, will ask the server to place the item in the slot to avoid desync issues.
    public void setFromClient(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        PacketDistributor.sendToServer(new CSetFilterSlotItemPacket(copy, this.getComponent().getManager().getTile().getBlockPos(), this.getComponent().getId()));
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        this.getComponent().setItemStack(copy);
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }

    @Override
    public Optional<ItemStack> tryRemove(int count, int decrement, Player player) {
        this.getComponent().setItemStack(ItemStack.EMPTY);
        return Optional.empty();
    }
}
