package fr.frinn.custommachinery.forge.transfer;

import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.Supplier;

public class SidedEnergyStorage implements IEnergyStorage {

    private final Supplier<SideMode> mode;
    private final EnergyMachineComponent component;

    public SidedEnergyStorage(Supplier<SideMode> mode, EnergyMachineComponent component) {
        this.mode = mode;
        this.component = component;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return this.mode.get().isInput() ? Utils.toInt(this.component.receiveEnergy(maxReceive, simulate)) : 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return this.mode.get().isOutput() ? Utils.toInt(this.component.extractEnergy(maxExtract, simulate)) : 0;
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
        return this.mode.get().isOutput();
    }

    @Override
    public boolean canReceive() {
        return this.mode.get().isInput();
    }
}
