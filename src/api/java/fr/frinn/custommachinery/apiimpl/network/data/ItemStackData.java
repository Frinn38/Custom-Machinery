package fr.frinn.custommachinery.apiimpl.network.data;

import fr.frinn.custommachinery.api.network.DataType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class ItemStackData extends Data<ItemStack> {

    private ItemStack value;

    public ItemStackData(short id, ItemStack value) {
        super(DataType.ITEMSTACK_DATA.get(), id);
        this.value = value;
    }

    public ItemStackData(short id, PacketBuffer buffer) {
        this(id, buffer.readItemStack());
    }

    @Override
    public void writeData(PacketBuffer buffer) {
        super.writeData(buffer);
        buffer.writeItemStack(this.value);
    }

    @Override
    public ItemStack getValue() {
        return this.value;
    }
}
