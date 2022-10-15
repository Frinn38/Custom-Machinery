package fr.frinn.custommachinery.fabric.transfer;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.fabric.FluidStackHooksFabric;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class FluidTank extends SnapshotParticipant<FluidStack> implements SingleSlotStorage<FluidVariant> {

    private final FluidMachineComponent component;
    @Nullable
    private final Direction side;

    public FluidTank(FluidMachineComponent component, @Nullable Direction side) {
        this.component = component;
        this.side = side;
    }

    public FluidMachineComponent getComponent() {
        return this.component;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if(this.side != null && !this.component.getConfig().getSideMode(this.side).isInput())
            return 0;

        if(!this.component.isFluidValid(FluidStackHooksFabric.fromFabric(resource, maxAmount)))
            return 0;

        long inserted = this.component.insert(resource.getFluid(), maxAmount, resource.getNbt(), true);
        if(inserted > 0) {
            updateSnapshots(transaction);
            this.component.insert(resource.getFluid(), maxAmount, resource.getNbt(), false);
            return inserted;
        }
        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if(this.side != null && !this.component.getConfig().getSideMode(this.side).isOutput())
            return 0;


        if(this.component.getFluidStack().getFluid() != resource.getFluid() || !this.component.getFluidStack().isTagEqual(FluidStackHooksFabric.fromFabric(resource, maxAmount)))
            return 0;

        long extracted = this.component.extract(maxAmount, true).getAmount();
        if(extracted > 0) {
            updateSnapshots(transaction);
            this.component.extract(maxAmount, false);
            return extracted;
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return this.component.getFluidStack().isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        return FluidStackHooksFabric.toFabric(this.component.getFluidStack());
    }

    @Override
    public long getAmount() {
        return this.component.getFluidStack().getAmount();
    }

    @Override
    public long getCapacity() {
        return this.component.getCapacity();
    }

    @Override
    protected FluidStack createSnapshot() {
        return this.component.getFluidStack().copy();
    }

    @Override
    protected void readSnapshot(FluidStack snapshot) {
        this.component.setFluidStack(snapshot);
    }

    @Override
    protected void onFinalCommit() {
        this.component.getManager().markDirty();
    }
}
