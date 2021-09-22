package fr.frinn.custommachinery.common.network.sync;

import fr.frinn.custommachinery.common.network.sync.data.ItemStackData;
import fr.frinn.custommachinery.impl.network.AbstractSyncable;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ItemStackSyncable extends AbstractSyncable<ItemStackData, ItemStack> {

    @Override
    public ItemStackData getData(short id) {
        return new ItemStackData(id, get());
    }

    @Override
    public boolean needSync() {
        ItemStack value = get();
        boolean needSync;
        if(this.lastKnownValue != null)
            needSync = !ItemStack.areItemStacksEqual(value, this.lastKnownValue);
        else needSync = true;
        this.lastKnownValue = value.copy();
        return needSync;
    }

    public static ItemStackSyncable create(Supplier<ItemStack> supplier, Consumer<ItemStack> consumer) {
        return new ItemStackSyncable() {
            @Override
            public ItemStack get() {
                return supplier.get();
            }

            @Override
            public void set(ItemStack value) {
                consumer.accept(value);
            }
        };
    }
}
