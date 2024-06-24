package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class SidedEnergyStorage implements IEnergyStorage {

    private final Direction side;
    private final EnergyMachineComponent component;

    public SidedEnergyStorage(Direction side, EnergyMachineComponent component) {
        this.side = side;
        this.component = component;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return this.component.getConfig().getSideMode(this.side).isInput() ? this.component.receiveEnergy(maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return this.component.getConfig().getSideMode(this.side).isOutput() ? this.component.extractEnergy(maxExtract, simulate) : 0;
    }

    @Override
    public int getEnergyStored() {
        return Utils.toInt(this.component.getEnergy());
    }

    @Override
    public int getMaxEnergyStored() {
        return Utils.toInt(this.component.getCapacity());
    }

    @Override
    public boolean canExtract() {
        return this.component.getConfig().getSideMode(this.side).isOutput();
    }

    @Override
    public boolean canReceive() {
        return this.component.getConfig().getSideMode(this.side).isInput();
    }
}
