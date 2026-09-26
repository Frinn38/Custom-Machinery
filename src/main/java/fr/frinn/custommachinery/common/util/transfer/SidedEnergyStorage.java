package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SidedEnergyStorage implements EnergyHandler {

    private final Direction side;
    private final EnergyMachineComponent component;

    public SidedEnergyStorage(Direction side, EnergyMachineComponent component) {
        this.side = side;
        this.component = component;
    }

    @Override
    public int insert(int amount, TransactionContext tx) {
        return this.component.getConfig().getDirectionMode(this.side).isInput() ? this.component.insert(amount, tx) : 0;
    }

    @Override
    public int extract(int amount, TransactionContext tx) {
        return this.component.getConfig().getDirectionMode(this.side).isOutput() ? this.component.extract(amount, tx) : 0;
    }

    @Override
    public long getAmountAsLong() {
        return this.component.getEnergy();
    }

    @Override
    public long getCapacityAsLong() {
        return this.component.getCapacity();
    }
}
