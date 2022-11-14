package fr.frinn.custommachinery.fabric.transfer;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class EnergyBuffer extends SnapshotParticipant<Long> implements EnergyStorage {

    @Nullable
    private final Direction side;
    private final EnergyMachineComponent component;

    public EnergyBuffer(EnergyMachineComponent component, @Nullable Direction side) {
        this.side = side;
        this.component = component;
    }

    @Override
    protected Long createSnapshot() {
        return this.component.getEnergy();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        this.component.setEnergy(snapshot);
    }

    @Override
    protected void onFinalCommit() {
        this.component.getManager().markDirty();
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        if(this.side != null && !this.component.getConfig().getSideMode(this.side).isInput())
            return 0;

        long maxInsert = this.component.receiveEnergy(maxAmount, true);

        if(maxInsert > 0) {
            updateSnapshots(transaction);
            this.component.receiveEnergy(maxAmount, false);
            return maxInsert;
        }
        return 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        if(this.side != null && !this.component.getConfig().getSideMode(this.side).isOutput())
            return 0;

        long maxExtract = this.component.extractEnergy(maxAmount, true);

        if(maxExtract > 0) {
            updateSnapshots(transaction);
            this.component.extractEnergy(maxAmount, false);
            return maxExtract;
        }
        return 0;
    }

    @Override
    public long getAmount() {
        return this.component.getEnergy();
    }

    @Override
    public long getCapacity() {
        return this.component.getCapacity();
    }

    @Override
    public boolean supportsInsertion() {
        return this.side != null && this.component.getConfig().getSideMode(this.side).isInput();
    }

    @Override
    public boolean supportsExtraction() {
        return this.side != null && this.component.getConfig().getSideMode(this.side).isOutput();
    }
}
