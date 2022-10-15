package fr.frinn.custommachinery.fabric.transfer;

import fr.frinn.custommachinery.common.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.util.Utils;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class ItemSlot extends SnapshotParticipant<ItemStack> implements SingleSlotStorage<ItemVariant> {

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
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if(this.side != null && !this.component.getConfig().getSideMode(this.side).isInput())
            return 0;

        if(!this.component.isItemValid(resource.toStack()))
            return 0;

        long inserted = this.component.insert(resource.getItem(), Utils.toInt(maxAmount), resource.getNbt(), true);
        if(inserted > 0) {
            updateSnapshots(transaction);
            this.component.insert(resource.getItem(), Utils.toInt(maxAmount), resource.getNbt(), false);
            return inserted;
        }
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if(this.side != null && !this.component.getConfig().getSideMode(this.side).isOutput())
            return 0;


        if(this.component.getItemStack().getItem() != resource.getItem() || !ItemStack.tagMatches(this.component.getItemStack(), resource.toStack()))
            return 0;

        long extracted = this.component.extract(Utils.toInt(maxAmount), true).getCount();
        if(extracted > 0) {
            updateSnapshots(transaction);
            this.component.extract(Utils.toInt(maxAmount), false);
            return extracted;
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return this.component.getItemStack().isEmpty();
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(this.component.getItemStack());
    }

    @Override
    public long getAmount() {
        return this.component.getItemStack().getCount();
    }

    @Override
    public long getCapacity() {
        return this.component.getCapacity();
    }

    @Override
    protected ItemStack createSnapshot() {
        return this.component.getItemStack().copy();
    }

    @Override
    protected void readSnapshot(ItemStack snapshot) {
        this.component.setItemStack(snapshot);
    }

    @Override
    protected void onFinalCommit() {
        this.component.getManager().markDirty();
    }
}
