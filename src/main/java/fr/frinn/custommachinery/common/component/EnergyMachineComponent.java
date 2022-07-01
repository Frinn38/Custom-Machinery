package fr.frinn.custommachinery.common.component;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.ICapabilityComponent;
import fr.frinn.custommachinery.api.component.IComparatorInputComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.apiimpl.codec.CodecLogger;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.apiimpl.component.config.RelativeSide;
import fr.frinn.custommachinery.apiimpl.component.config.SideConfig;
import fr.frinn.custommachinery.apiimpl.component.config.SideMode;
import fr.frinn.custommachinery.apiimpl.integration.jei.Energy;
import fr.frinn.custommachinery.common.component.config.SidedEnergyStorage;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.syncable.LongSyncable;
import fr.frinn.custommachinery.common.network.syncable.SideConfigSyncable;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EnergyMachineComponent extends AbstractMachineComponent implements IEnergyStorage, ITickableComponent, ICapabilityComponent, ISerializableComponent, ISyncableStuff, IComparatorInputComponent, ISideConfigComponent {

    private long energy;
    private final long capacity;
    private final int maxInput;
    private final int maxOutput;
    private final SideConfig config;
    private long actualTick;
    private int actualTickInput;
    private int actualTickOutput;
    private int searchNeighborCooldown = Utils.RAND.nextInt(20);
    private final Map<Direction, LazyOptional<IEnergyStorage>> sidedWrappers = Maps.newEnumMap(Direction.class);
    private final LazyOptional<EnergyMachineComponent> capability = LazyOptional.of(() -> this);
    private final Map<Direction, LazyOptional<IEnergyStorage>> neighborStorages = new HashMap<>();

    public EnergyMachineComponent(IMachineComponentManager manager, long capacity, int maxInput, int maxOutput, Map<RelativeSide, SideMode> defaultConfig) {
        super(manager, ComponentIOMode.BOTH);
        this.energy = 0;
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.config = new SideConfig(this, defaultConfig);
        for(Direction direction : Direction.values())
            this.sidedWrappers.put(direction, LazyOptional.of(() -> new SidedEnergyStorage(() -> config.getSideMode(direction), this)));
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
        if(getManager().getTile().isPaused() || this.energy == 0)
            return;

        AtomicInteger maxExtract = new AtomicInteger(extractEnergy(Integer.MAX_VALUE, true));

        if(maxExtract.get() > 0) {
            if(this.searchNeighborCooldown-- <= 0) {
                this.searchNeighborCooldown = 20;
                this.searchNeighborStorages();
            }
            this.neighborStorages.values().forEach(cap ->
                cap.ifPresent(energy -> {
                    int toInsert = energy.receiveEnergy(maxExtract.get(), true);
                    if (toInsert > 0) {
                        this.extractEnergy(toInsert, false);
                        energy.receiveEnergy(toInsert, false);
                        maxExtract.addAndGet(-toInsert);
                    }
                })
            );
        }
    }

    private void searchNeighborStorages() {
        for(Direction direction : Direction.values()) {
            if(this.neighborStorages.get(direction) != null)
                continue;
            if(!this.config.getSideMode(direction).isOutput())
                continue;
            Level world = getManager().getWorld();
            BlockPos pos = getManager().getTile().getBlockPos();
            LazyOptional<IEnergyStorage> neighborStorage = Optional.ofNullable(world.getBlockEntity(pos.relative(direction))).map(tile -> tile.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite())).orElse(null);
            if(neighborStorage != null && neighborStorage.isPresent()) {
                neighborStorage.addListener(storage -> this.neighborStorages.remove(direction));
                this.neighborStorages.put(direction, neighborStorage);
            }
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityEnergy.ENERGY) {
            if(side == null)
                return this.capability.cast();
            else
                return this.sidedWrappers.get(side).cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public void invalidateCapability() {
        this.capability.invalidate();
        this.sidedWrappers.values().forEach(LazyOptional::invalidate);
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

    /** ENERGY STORAGE STUFF **/

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        if(this.actualTick != this.getManager().getWorld().getGameTime()) {
            this.actualTick = this.getManager().getWorld().getGameTime();
            this.actualTickInput = 0;
            this.actualTickOutput = 0;
        }

        int maxTickInput = this.maxInput - this.actualTickInput;

        int energyReceived = Math.min(Utils.toInt(this.capacity - this.energy), Math.min(maxTickInput, maxReceive));
        if (!simulate && energyReceived > 0) {
            this.energy += energyReceived;
            this.actualTickInput += energyReceived;
            getManager().markDirty();
        }

        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        if(this.actualTick != this.getManager().getWorld().getGameTime()) {
            this.actualTick = this.getManager().getWorld().getGameTime();
            this.actualTickInput = 0;
            this.actualTickOutput = 0;
        }

        int maxTickOutput = this.maxOutput - this.actualTickOutput;

        int energyExtracted = Math.min(Utils.toInt(this.energy), Math.min(maxTickOutput, maxExtract));
        if (!simulate) {
            this.energy -= energyExtracted;
            this.actualTickOutput += energyExtracted;
            getManager().markDirty();
        }

        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return Utils.toInt(this.energy);
    }

    @Override
    public int getMaxEnergyStored() {
        return Utils.toInt(this.capacity);
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    /** Recipe Stuff **/

    public int receiveRecipeEnergy(int maxReceive, boolean simulate) {
        if(!canReceive())
            return 0;

        int energyReceived = Math.min(Utils.toInt(this.capacity - this.energy), maxReceive);
        if(!simulate) {
            this.energy += energyReceived;
            getManager().markDirty();
        }
        return energyReceived;
    }

    public int extractRecipeEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

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
                        CodecLogger.loggedOptional(Codecs.SIDE_CONFIG_CODEC, "config", SideConfig.DEFAULT_ALL_BOTH).forGetter(template -> template.defaultConfig)
                ).apply(templateInstance, (capacity, maxInput, maxOutput, config) -> new EnergyMachineComponent.Template(capacity, maxInput.orElse(Utils.toInt(capacity)), maxOutput.orElse(Utils.toInt(capacity)), config))
        );

        private final long capacity;
        private final int maxInput;
        private final int maxOutput;
        private final Map<RelativeSide, SideMode> defaultConfig;

        public Template(long capacity, int maxInput, int maxOutput, Map<RelativeSide, SideMode> defaultConfig) {
            this.capacity = capacity;
            this.maxInput = maxInput;
            this.maxOutput = maxOutput;
            this.defaultConfig = defaultConfig;
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
            return new EnergyMachineComponent(manager, this.capacity, this.maxInput, this.maxOutput, this.defaultConfig);
        }
    }
}
