package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ItemStackData extends Data<ItemStack> {
    public ItemStackData(short id, ItemStack value) {
        super(Registration.ITEMSTACK_DATA.get(), id, value);
    }

    public ItemStackData(short id, RegistryFriendlyByteBuf buffer) {
        this(id, ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
    }

    @Override
    public void writeData(RegistryFriendlyByteBuf buffer) {
        super.writeData(buffer);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, getValue());
    }
}
