package fr.frinn.custommachinery.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.PlatformHelper;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IComparatorInputComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.syncable.LongSyncable;
import fr.frinn.custommachinery.common.network.syncable.SideConfigSyncable;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.transfer.ICommonEnergyHandler;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import fr.frinn.custommachinery.impl.integration.jei.Energy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;
import java.util.function.Consumer;

public class EnergyMachineComponent extends AbstractMachineComponent implements ITickableComponent, ISerializableComponent, ISyncableStuff, IComparatorInputComponent, ISideConfigComponent {

    private long energy;
    private final long capacity;
    private final int maxInput;
    private final int maxOutput;
    private final ICommonEnergyHandler handler;
    private final SideConfig config;

    private long actualTick;

    public EnergyMachineComponent(IMachineComponentManager manager, long capacity, int maxInput, int maxOutput, SideConfig.Template configTemplate) {
        super(manager, ComponentIOMode.BOTH);
        this.energy = 0;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.handler = PlatformHelper.createEnergyHandler(this);
        this.config = configTemplate.build(this);
        this.config.setCallback(this.handler::configChanged);
    }

    public int getMaxInput() {
        return this.maxInput;
    }

    public int getMaxOutput() {
        return this.maxOutput;
    }

    //For GUI element rendering
    public double getFillPercent() {
        return (double)this.energy / this.capacity;
    }

    public long getEnergy() {
        return this.energy;
    }

    public long getCapacity() {
        return this.capacity;
    }

    public void setEnergy(long energy) {
        this.energy = energy;
        getManager().markDirty();
    }

    public long receiveEnergy(long maxReceive, boolean simulate) {
        if (this.getMaxInput() <= 0)
            return 0;

        long energyReceived = Math.min(this.getCapacity() - this.getEnergy(), Math.min(this.getMaxInput(), maxReceive));
        if (!simulate && energyReceived > 0) {
            this.setEnergy(this.getEnergy() + energyReceived);
            this.getManager().markDirty();
        }

        return energyReceived;
    }

    public long extractEnergy(long maxExtract, boolean simulate) {
        if (this.getMaxOutput() <= 0)
            return 0;

        long energyExtracted = Math.min(this.getEnergy(), Math.min(this.getMaxOutput(), maxExtract));
        if (!simulate && energyExtracted > 0) {
            this.setEnergy(this.getEnergy() - energyExtracted);
            this.getManager().markDirty();
        }

        return energyExtracted;
    }

    public ICommonEnergyHandler getEnergyHandler() {
        return this.handler;
    }

    @Override
    public SideConfig getConfig() {
        return this.config;
    }

    @Override
    public String getId() {
        return "energy";
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public void serverTick() {
        this.handler.tick();
    }

    @Override
    public void onRemoved() {
        this.handler.invalidate();
    }

    @Override
    public void serialize(CompoundTag nbt) {
        nbt.putLong("energy", this.energy);
        nbt.put("config", this.config.serialize());
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        if(nbt.contains("energy", Tag.TAG_LONG))
            this.energy = Math.min(nbt.getLong("energy"), this.capacity);
        if(nbt.contains("config"))
            this.config.deserialize(nbt.get("config"));
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(LongSyncable.create(() -> this.energy, energy -> this.energy = energy));
        container.accept(SideConfigSyncable.create(this::getConfig, this.config::set));
    }

    @Override
    public int getComparatorInput() {
        return (int) (15 * ((double)this.energy / (double)this.capacity));
    }

    /** Recipe Stuff **/

    public int receiveRecipeEnergy(int maxReceive, boolean simulate) {
        int energyReceived = Math.min(Utils.toInt(this.capacity - this.energy), maxReceive);
        if(!simulate) {
            this.energy += energyReceived;
            getManager().markDirty();
        }
        return energyReceived;
    }

    public int extractRecipeEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(Utils.toInt(this.energy), maxExtract);
        if (!simulate) {
            this.energy -= energyExtracted;
            getManager().markDirty();
        }
        return energyExtracted;
    }

    public static class Template implements IMachineComponentTemplate<EnergyMachineComponent> {

        public static final Codec<Template> CODEC = RecordCodecBuilder.create(templateInstance ->
                templateInstance.group(
                        Codecs.longRange(1, Long.MAX_VALUE).fieldOf("capacity").forGetter(template -> template.capacity),
                        CodecLogger.loggedOptional(Codec.intRange(0, Integer.MAX_VALUE),"maxInput").forGetter(template -> Optional.of(template.maxInput)),
                        CodecLogger.loggedOptional(Codec.intRange(0, Integer.MAX_VALUE),"maxOutput").forGetter(template -> Optional.of(template.maxOutput)),
                        CodecLogger.loggedOptional(SideConfig.Template.CODEC, "config", SideConfig.Template.DEFAULT_ALL_INPUT).forGetter(template -> template.config)
                ).apply(templateInstance, (capacity, maxInput, maxOutput, config) -> new EnergyMachineComponent.Template(capacity, maxInput.orElse(Utils.toInt(capacity)), maxOutput.orElse(Utils.toInt(capacity)), config))
        );

        private final long capacity;
        private final int maxInput;
        private final int maxOutput;
        private final SideConfig.Template config;

        public Template(long capacity, int maxInput, int maxOutput, SideConfig.Template config) {
            this.capacity = capacity;
            this.maxInput = maxInput;
            this.maxOutput = maxOutput;
            this.config = config;
        }

        @Override
        public MachineComponentType<EnergyMachineComponent> getType() {
            return Registration.ENERGY_MACHINE_COMPONENT.get();
        }

        @Override
        public String getId() {
            return "";
        }

        @Override
        public boolean canAccept(Object ingredient, boolean isInput, IMachineComponentManager manager) {
            return ingredient instanceof Energy;
        }

        @Override
        public EnergyMachineComponent build(IMachineComponentManager manager) {
            return new EnergyMachineComponent(manager, this.capacity, this.maxInput, this.maxOutput, this.config);
        }
    }
}
