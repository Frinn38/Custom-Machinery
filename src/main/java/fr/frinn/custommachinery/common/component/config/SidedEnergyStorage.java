package fr.frinn.custommachinery.common.component.config;

import fr.frinn.custommachinery.apiimpl.component.config.SideMode;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.Supplier;

public class SidedEnergyStorage implements IEnergyStorage {

    private final Supplier<SideMode> mode;
    private final IEnergyStorage wrapped;

    public SidedEnergyStorage(Supplier<SideMode> mode, IEnergyStorage wrapped) {
        this.mode = mode;
        this.wrapped = wrapped;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return this.mode.get().isInput() ? this.wrapped.receiveEnergy(maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return this.mode.get().isOutput() ? this.wrapped.extractEnergy(maxExtract, simulate) : 0;
    }

    @Override
    public int getEnergyStored() {
        return this.wrapped.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return this.wrapped.getMaxEnergyStored();
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
