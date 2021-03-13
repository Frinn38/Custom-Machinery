package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.init.Registration;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class EnergyMachineComponent extends AbstractMachineComponent implements IEnergyStorage, ICapabilityMachineComponent {

    private int energy;
    private int capacity;
    private int maxInput;
    private int maxOutput;
    private LazyOptional<EnergyMachineComponent> capability = LazyOptional.of(() -> this);

    public EnergyMachineComponent(MachineComponentManager manager, Mode mode, int capacity, int maxInput, int maxOutput) {
        super(manager, mode);
        this.energy = 0;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityEnergy.ENERGY)
            return this.capability.cast();
        return LazyOptional.empty();
    }

    @Override
    public void invalidateCapability() {
        this.capability.invalidate();
    }

    @Override
    public void serialize(CompoundNBT nbt) {
        nbt.putInt("energy", this.energy);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        if(nbt.contains("energy", Constants.NBT.TAG_INT))
            this.energy = nbt.getInt("energy");
    }

    @Override
    public void addProbeInfo(IProbeInfo info) {
        info.progress(this.energy, this.capacity);
    }

    /** ENERGY STORAGE STUFF **/

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        int energyReceived = Math.min(capacity - energy, Math.min(this.maxInput, maxReceive));
        if (!simulate)
            energy += energyReceived;
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        int energyExtracted = Math.min(energy, Math.min(this.maxOutput, maxExtract));
        if (!simulate)
            energy -= energyExtracted;
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public static class Template implements IMachineComponentTemplate<EnergyMachineComponent> {

        public static final Codec<Template> CODEC = RecordCodecBuilder.create(templateInstance ->
                templateInstance.group(
                        Codec.INT.fieldOf("capacity").forGetter(template -> template.capacity),
                        Codec.INT.optionalFieldOf("maxInput").forGetter(template -> Optional.of(template.maxInput)),
                        Codec.INT.optionalFieldOf("maxOutput").forGetter(template -> Optional.of(template.maxOutput))
                ).apply(templateInstance, (capacity, maxInput, maxOutput) -> new EnergyMachineComponent.Template(capacity, maxInput.orElse(capacity), maxOutput.orElse(capacity)))
        );

        private int capacity;
        private int maxInput;
        private int maxOutput;

        public Template(int capacity, int maxInput, int maxOutput) {
            this.capacity = capacity;
            this.maxInput = maxInput;
            this.maxOutput = maxOutput;
        }

        @Override
        public MachineComponentType<EnergyMachineComponent> getType() {
            return Registration.ENERGY_MACHINE_COMPONENT.get();
        }

        @Override
        public EnergyMachineComponent build(MachineComponentManager manager) {
            return new EnergyMachineComponent(manager, Mode.BOTH, this.capacity, this.maxInput, this.maxOutput);
        }
    }
}
