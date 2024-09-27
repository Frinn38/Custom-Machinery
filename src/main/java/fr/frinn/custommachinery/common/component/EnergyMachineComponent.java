package fr.frinn.custommachinery.common.component;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IComparatorInputComponent;
import fr.frinn.custommachinery.api.component.IDumpComponent;
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
import fr.frinn.custommachinery.common.network.syncable.IOSideConfigSyncable;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.transfer.SidedEnergyStorage;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import fr.frinn.custommachinery.impl.component.config.IOSideMode;
import fr.frinn.custommachinery.impl.integration.jei.Energy;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class EnergyMachineComponent extends AbstractMachineComponent implements ITickableComponent, ISerializableComponent, ISyncableStuff, IComparatorInputComponent, ISideConfigComponent, IDumpComponent, IEnergyStorage {

    private long energy;
    private final long capacity;
    private final long maxInput;
    private final long maxOutput;
    private final IOSideConfig config;
    private final Map<Direction, SidedEnergyStorage> sidedStorages = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

    public EnergyMachineComponent(IMachineComponentManager manager, long capacity, long maxInput, long maxOutput, IOSideConfig.Template configTemplate) {
        super(manager, ComponentIOMode.BOTH);
        this.energy = 0;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.config = configTemplate.build(this);
        this.config.setCallback(this::configChanged);
        for(Direction side : Direction.values())
            this.sidedStorages.put(side, new SidedEnergyStorage(side, this));
    }

    public long getMaxInput() {
        return this.maxInput;
    }

    public long getMaxOutput() {
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

    public void configChanged(RelativeSide side, IOSideMode oldMode, IOSideMode newMode) {
        if(oldMode.isNone() != newMode.isNone())
            this.getManager().getTile().invalidateCapabilities();
    }

    @Nullable
    public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        if(side == null)
            return this;
        if(!this.config.getSideMode(side).isNone())
            return this.sidedStorages.get(side);
        return null;
    }

    @Override
    public IOSideConfig getConfig() {
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
        for(Direction side : Direction.values()) {
            if(this.getConfig().getSideMode(side) == IOSideMode.NONE)
                continue;

            IEnergyStorage neighbour;

            if(this.neighbourStorages.get(side) == null) {
                this.neighbourStorages.put(side, BlockCapabilityCache.create(EnergyStorage.BLOCK, (ServerLevel)this.getManager().getLevel(), this.getManager().getTile().getBlockPos().relative(side), side.getOpposite(), () -> !this.getManager().getTile().isRemoved(), () -> this.neighbourStorages.remove(side)));
                if(this.neighbourStorages.get(side) != null)
                    neighbour = this.neighbourStorages.get(side).getCapability();
                else
                    continue;
            }
            else
                neighbour = this.neighbourStorages.get(side).getCapability();

            if(neighbour == null)
                continue;

            if(this.getConfig().isAutoInput() && this.getConfig().getSideMode(side).isInput() && this.getEnergy() < this.getCapacity())
                move(neighbour, this.sidedStorages.get(side), Integer.MAX_VALUE);

            if(this.getConfig().isAutoOutput() && this.getConfig().getSideMode(side).isOutput() && this.getEnergy() > 0)
                move(this.sidedStorages.get(side), neighbour, Integer.MAX_VALUE);
        }
    }

    private void move(IEnergyStorage from, IEnergyStorage to, int maxAmount) {
        int maxExtracted = from.extractEnergy(maxAmount, true);
        if(maxExtracted > 0) {
            int maxInserted = to.receiveEnergy(maxExtracted, true);
            if(maxInserted > 0) {
                from.extractEnergy(maxInserted, false);
                to.receiveEnergy(maxExtracted, false);
            }
        }
    }

    @Override
    public void serialize(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putLong("energy", this.energy);
        nbt.put("config", this.config.serialize());
    }

    @Override
    public void deserialize(CompoundTag nbt, HolderLookup.Provider registries) {
        if(nbt.contains("energy", Tag.TAG_LONG))
            this.energy = Math.min(nbt.getLong("energy"), this.capacity);
        if(nbt.contains("config"))
            this.config.deserialize(nbt.getCompound("config"));
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(LongSyncable.create(() -> this.energy, energy -> this.energy = energy));
        container.accept(IOSideConfigSyncable.create(this::getConfig, this.config::set));
    }

    @Override
    public int getComparatorInput() {
        return (int) (15 * ((double)this.energy / (double)this.capacity));
    }

    @Override
    public void dump(List<String> ids) {
        setEnergy(0L);
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

    /** IEnergyStorage Stuff **/

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (this.getMaxInput() <= 0)
            return 0;

        int energyReceived = (int)Math.min(this.getCapacity() - this.getEnergy(), Math.min(this.getMaxInput(), toReceive));
        if (!simulate && energyReceived > 0) {
            this.setEnergy(this.getEnergy() + energyReceived);
            this.getManager().markDirty();
        }

        return energyReceived;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (this.getMaxOutput() <= 0)
            return 0;

        long energyExtracted = Math.min(this.getEnergy(), Math.min(this.getMaxOutput(), toExtract));
        if (!simulate && energyExtracted > 0) {
            this.setEnergy(this.getEnergy() - energyExtracted);
            this.getManager().markDirty();
        }

        return (int)energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return (int)this.getEnergy();
    }

    @Override
    public int getMaxEnergyStored() {
        return (int)this.getCapacity();
    }

    @Override
    public boolean canExtract() {
        return this.getEnergy() > 0 && this.getMaxOutput() > 0;
    }

    @Override
    public boolean canReceive() {
        return this.getCapacity() - this.getEnergy() > 0 && this.getMaxInput() > 0;
    }

    public record Template(
            long capacity,
            long maxInput,
            long maxOutput,
            IOSideConfig.Template config
    ) implements IMachineComponentTemplate<EnergyMachineComponent> {

        public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
                templateInstance.group(
                        NamedCodec.longRange(1, Long.MAX_VALUE).fieldOf("capacity").forGetter(template -> template.capacity),
                        NamedCodec.longRange(0, Long.MAX_VALUE).optionalFieldOf("maxInput").forGetter(template -> template.maxInput == template.capacity ? Optional.empty() : Optional.of(template.maxInput)),
                        NamedCodec.longRange(0, Long.MAX_VALUE).optionalFieldOf("maxOutput").forGetter(template -> template.maxOutput == template.capacity ? Optional.empty() : Optional.of(template.maxOutput)),
                        IOSideConfig.Template.CODEC.optionalFieldOf("config", IOSideConfig.Template.DEFAULT_ALL_INPUT).forGetter(template -> template.config)
                ).apply(templateInstance, (capacity, maxInput, maxOutput, config) ->
                        new EnergyMachineComponent.Template(capacity, maxInput.orElse(capacity), maxOutput.orElse(capacity), config)
                ), "Energy machine component"
        );

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
