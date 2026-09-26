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
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.network.syncable.IOSideConfigSyncable;
import fr.frinn.custommachinery.common.network.syncable.LongSyncable;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.transfer.SidedEnergyStorage;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import fr.frinn.custommachinery.impl.component.config.IOSideMode;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.integration.jei.Energy;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnergyMachineComponent extends AbstractMachineComponent implements ITickableComponent, ISerializableComponent, ISyncableStuff, IComparatorInputComponent, ISideConfigComponent, IDumpComponent, EnergyHandler {

    private long energy;
    private long clientCapacity;
    private final Supplier<Long> capacity;
    private final Supplier<Long> maxInput;
    private final Supplier<Long> minInput;
    private final Supplier<Long> maxOutput;
    private final Supplier<Long> minOutput;
    private final IOSideConfig config;
    private final EnergyComponentSnapshot snapshot;
    private final Map<Direction, SidedEnergyStorage> sidedStorages = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockCapabilityCache<EnergyHandler, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

    public EnergyMachineComponent(IMachineComponentManager manager, long capacity, long maxInput, long minInput, long maxOutput, long minOutput, IOSideConfig.Template configTemplate) {
        super(manager, ComponentIOMode.BOTH);
        this.energy = 0;
        this.capacity = this.upgradeableL(capacity, "capacity", 1, Long.MAX_VALUE, value -> this.energy = Math.min(this.energy, value));
        this.maxInput = this.upgradeableL(maxInput, "max_input", 0, Long.MAX_VALUE);
        this.minInput = this.upgradeableL(minInput, "min_input", 0, Long.MAX_VALUE);
        this.maxOutput = this.upgradeableL(maxOutput, "max_output", 0, Long.MAX_VALUE);
        this.minOutput = this.upgradeableL(minOutput, "min_output", 0, Long.MAX_VALUE);
        this.config = configTemplate.build(manager.facing());
        this.config.setCallback(this::configChanged);
        this.snapshot = new EnergyComponentSnapshot();
        for(Direction side : Direction.values())
            this.sidedStorages.put(side, new SidedEnergyStorage(side, this));
        this.clientCapacity = capacity;
    }

    public long getMaxInput() {
        return this.maxInput.get();
    }

    public long getMinInput() {
        return this.minInput.get();
    }

    public long getMaxOutput() {
        return this.maxOutput.get();
    }

    public long getMinOutput() {
        return this.minOutput.get();
    }

    //For GUI element rendering
    public double getFillPercent() {
        return (double)this.energy / this.clientCapacity;
    }

    public long getClientCapacity() {
        return this.clientCapacity;
    }

    public long getEnergy() {
        return this.energy;
    }

    public long getCapacity() {
        return this.capacity.get();
    }

    public void setEnergy(long energy) {
        this.energy = energy;
        this.getManager().markDirty();
    }

    public void configChanged(RelativeSide side, IOSideMode oldMode, IOSideMode newMode) {
        if(oldMode.isNone() != newMode.isNone())
            this.getManager().getTile().invalidateCapabilities();
    }

    @Nullable
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        if(side == null)
            return this;
        if(!this.config.getDirectionMode(side).isNone())
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
        return CMRegistration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public void serverTick() {
        for(Direction side : Direction.values()) {
            if(!this.getConfig().canAutoIO(side))
                continue;

            if(this.neighbourStorages.get(side) == null)
                this.neighbourStorages.put(side, BlockCapabilityCache.create(Capabilities.Energy.BLOCK, (ServerLevel)this.getManager().getLevel(), this.getManager().getTile().getBlockPos().relative(side), side.getOpposite(), () -> !this.getManager().getTile().isRemoved(), () -> this.neighbourStorages.remove(side)));

            EnergyHandler neighbour = this.neighbourStorages.get(side).getCapability();

            if(neighbour == null)
                continue;

            if(this.getConfig().isAutoInput() && this.getConfig().getDirectionMode(side).isInput() && this.getEnergy() < this.getCapacity())
                move(neighbour, this.sidedStorages.get(side));

            if(this.getConfig().isAutoOutput() && this.getConfig().getDirectionMode(side).isOutput() && this.getEnergy() > 0)
                move(this.sidedStorages.get(side), neighbour);
        }
    }

    private void move(EnergyHandler from, EnergyHandler to) {
        try(Transaction tx = Transaction.openRoot()) {
            int maxExtracted = from.extract(Integer.MAX_VALUE, tx);
            if(maxExtracted > 0) {
                int maxInserted = to.insert(maxExtracted, tx);
                int toTransfer = maxInserted;
                if(maxInserted != maxExtracted) //Check in case 'from' can not accept to extract a lower value like our 'minOutput'.
                    toTransfer = from.extract(maxInserted, tx);
                if(toTransfer != maxInserted) //Check in case 'to' can not accept to insert a lower value like out 'minInput'.
                    toTransfer = to.insert(toTransfer, tx);
                if(toTransfer > 0) {
                    tx.commit();
                }
            }
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putLong("energy", this.energy);
        this.config.serialize(output.child("config"));
    }

    @Override
    public void deserialize(ValueInput input) {
        input.getLong("energy").ifPresent(energy -> this.energy = Math.min(energy, this.capacity.get()));
        input.child("config").ifPresent(this.config::deserialize);
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(LongSyncable.create(() -> this.energy, energy -> this.energy = energy));
        container.accept(LongSyncable.create(this.capacity, capacity -> this.clientCapacity = capacity));
        container.accept(IOSideConfigSyncable.create(this::getConfig, this.config::set));
    }

    @Override
    public int getComparatorInput() {
        return (int) (15 * ((double)this.energy / (double)this.capacity.get()));
    }

    @Override
    public void dump(List<String> ids) {
        this.setEnergy(0L);
    }

    /** Recipe Stuff **/

    public int receiveRecipeEnergy(int maxReceive, boolean simulate) {
        int energyReceived = Math.min(Utils.toInt(this.capacity.get() - this.energy), maxReceive);
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

    /** EnergyHandler Stuff **/

    @Override
    public long getAmountAsLong() {
        return this.getEnergy();
    }

    @Override
    public long getCapacityAsLong() {
        return this.getCapacity();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if(this.getMaxInput() <= 0 || amount < this.getMinInput())
            return 0;

        int energyReceived = (int)Math.min(this.getCapacity() - this.getEnergy(), Math.min(this.getMaxInput(), amount));
        if(energyReceived > 0) {
            this.snapshot.updateSnapshots(transaction);
            this.energy += energyReceived;
        }

        return energyReceived;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        if(this.getMaxOutput() <= 0 || amount < this.getMinOutput())
            return 0;

        long energyExtracted = Math.min(this.getEnergy(), Math.min(this.getMaxOutput(), amount));
        if(energyExtracted > 0) {
            this.snapshot.updateSnapshots(transaction);
            this.energy -= energyExtracted;
        }

        return (int)energyExtracted;
    }

    private class EnergyComponentSnapshot extends SnapshotJournal<Long> {

        @Override
        protected Long createSnapshot() {
            return EnergyMachineComponent.this.energy;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            EnergyMachineComponent.this.energy = snapshot;
        }

        @Override
        protected void onRootCommit(Long originalState) {
            EnergyMachineComponent.this.getManager().markDirty();
        }
    }

    public record Template(
            long capacity,
            long maxInput,
            long minInput,
            long maxOutput,
            long minOutput,
            IOSideConfig.Template config
    ) implements IMachineComponentTemplate<EnergyMachineComponent> {

        public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
                templateInstance.group(
                        NamedCodec.longRange(1, Long.MAX_VALUE).fieldOf("capacity").forGetter(template -> template.capacity),
                        NamedCodec.longRange(0, Long.MAX_VALUE).optionalFieldOf("maxInput").forGetter(template -> template.maxInput == template.capacity ? Optional.empty() : Optional.of(template.maxInput)),
                        NamedCodec.longRange(0, Long.MAX_VALUE).optionalFieldOf("minInput", 0L).forGetter(template -> template.minInput),
                        NamedCodec.longRange(0, Long.MAX_VALUE).optionalFieldOf("maxOutput").forGetter(template -> template.maxOutput == template.capacity ? Optional.empty() : Optional.of(template.maxOutput)),
                        NamedCodec.longRange(0, Long.MAX_VALUE).optionalFieldOf("minOutput", 0L).forGetter(template -> template.minOutput),
                        IOSideConfig.Template.CODEC.optionalFieldOf("config", IOSideConfig.Template.DEFAULT_ALL_INPUT).forGetter(template -> template.config)
                ).apply(templateInstance, (capacity, maxInput, minInput, maxOutput, minOutput, config) ->
                        new EnergyMachineComponent.Template(capacity, maxInput.orElse(capacity), minInput, maxOutput.orElse(capacity), minOutput, config)
                ), "Energy machine component"
        );

        @Override
        public MachineComponentType<EnergyMachineComponent> getType() {
            return CMRegistration.ENERGY_MACHINE_COMPONENT.get();
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
            return new EnergyMachineComponent(manager, this.capacity, this.maxInput, this.minInput, this.maxOutput, this.minOutput, this.config);
        }
    }
}
