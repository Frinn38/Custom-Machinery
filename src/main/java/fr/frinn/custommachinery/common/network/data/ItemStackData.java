package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ItemStackData extends Data<ItemStack> {
    public ItemStackData(short id, ItemStack value) {
        super(Registration.ITEMSTACK_DATA.get(), id, value);
    }

    public ItemStackData(short id, FriendlyByteBuf buffer) {
        this(id, buffer.readItem());
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        buffer.writeItem(getValue());
    }
}
