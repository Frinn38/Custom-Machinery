package fr.frinn.custommachinery.common.component.handler;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.component.IDumpComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.transfer.SidedFluidHandler;
import fr.frinn.custommachinery.impl.component.AbstractComponentHandler;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FluidComponentHandler extends AbstractComponentHandler<FluidMachineComponent> implements ISerializableComponent, ISyncableStuff, ITickableComponent, IDumpComponent, IFluidHandler {

    private final Map<Direction, SidedFluidHandler> sidedHandlers = Maps.newEnumMap(Direction.class);
    private final Map<Direction, BlockCapabilityCache<IFluidHandler, Direction>> neighbourStorages = Maps.newEnumMap(Direction.class);

    public FluidComponentHandler(IMachineComponentManager manager, List<FluidMachineComponent> components) {
        super(manager, components);
        components.forEach(component -> {
            component.getConfig().setCallback(this::configChanged);
            if(component.getMode().isInput())
                this.inputs.add(component);
            if(component.getMode().isOutput())
                this.outputs.add(component);
        });
        for(Direction side : Direction.values())
            this.sidedHandlers.put(side, new SidedFluidHandler(side, this));
    }

    public void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode) {
        if(oldMode.isNone() != newMode.isNone())
            this.getManager().getTile().invalidateCapabilities();
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        if(side == null)
            return this;
        else if(this.getComponents().stream().anyMatch(component -> !component.getConfig().getSideMode(side).isNone()))
            return this.sidedHandlers.get(side);
        return null;
    }

    @Override
    public MachineComponentType<FluidMachineComponent> getType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public Optional<FluidMachineComponent> getComponentForID(String id) {
        return this.getComponents().stream().filter(component -> component.getId().equals(id)).findFirst();
    }

    @Override
    public void serverTick() {
        //I/O between the machine and neighbour blocks.
        for(Direction side : Direction.values()) {
            if(this.getComponents().stream().allMatch(component -> component.getConfig().getSideMode(side) == SideMode.NONE))
                continue;

            IFluidHandler neighbour;

            if(this.neighbourStorages.get(side) == null) {
                this.neighbourStorages.put(side, BlockCapabilityCache.create(FluidHandler.BLOCK, (ServerLevel)this.getManager().getLevel(), this.getManager().getTile().getBlockPos().relative(side), side.getOpposite(), () -> !this.getManager().getTile().isRemoved(), () -> this.neighbourStorages.remove(side)));
                if(this.neighbourStorages.get(side) != null)
                    neighbour = this.neighbourStorages.get(side).getCapability();
                else
                    continue;
            }
            else
                neighbour = this.neighbourStorages.get(side).getCapability();

            if(neighbour == null)
                continue;

            this.getComponents().forEach(component -> {
                if(component.getConfig().isAutoInput() && component.getConfig().getSideMode(side).isInput() && component.getFluid().getAmount() < component.getCapacity())
                    FluidUtil.tryFluidTransfer(this.sidedHandlers.get(side), neighbour, Integer.MAX_VALUE, true);

                if(component.getConfig().isAutoOutput() && component.getConfig().getSideMode(side).isOutput() && component.getFluid().getAmount() > 0)
                    FluidUtil.tryFluidTransfer(neighbour, this.sidedHandlers.get(side), Integer.MAX_VALUE, true);
            });
        }
    }

    @Override
    public void serialize(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag componentsNBT = new ListTag();
        this.getComponents().forEach(component -> {
            CompoundTag componentNBT = new CompoundTag();
            component.serialize(componentNBT, registries);
            componentNBT.putString("id", component.getId());
            componentsNBT.add(componentNBT);
        });
        nbt.put("fluids", componentsNBT);
    }

    @Override
    public void deserialize(CompoundTag nbt, HolderLookup.Provider registries) {
        if(nbt.contains("fluids", Tag.TAG_LIST)) {
            ListTag componentsNBT = nbt.getList("fluids", Tag.TAG_COMPOUND);
            componentsNBT.forEach(inbt -> {
                if(inbt instanceof CompoundTag componentNBT) {
                    if(componentNBT.contains("id", Tag.TAG_STRING)) {
                        this.getComponents().stream().filter(component -> component.getId().equals(componentNBT.getString("id"))).findFirst().ifPresent(component -> component.deserialize(componentNBT, registries));
                    }
                }
            });
        }
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        this.getComponents().forEach(component -> component.getStuffToSync(container));
    }

    @Override
    public void dump(List<String> ids) {
        this.getComponents().stream()
                .filter(component -> ids.contains(component.getId()))
                .forEach(component -> component.setFluidStack(FluidStack.EMPTY));
    }

    /** RECIPE STUFF **/

    private final List<FluidMachineComponent> inputs = new ArrayList<>();
    private final List<FluidMachineComponent> outputs = new ArrayList<>();

    public long getFluidAmount(String tank, Fluid fluid, @Nullable CompoundTag nbt) {
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        return this.inputs.stream().filter(component -> component.getFluid().getFluid() == fluid).mapToLong(component -> component.getFluid().getAmount()).sum();
    }

    public long getSpaceForFluid(String tank, Fluid fluid, @Nullable CompoundTag nbt) {
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        return this.outputs.stream().filter(component -> component.isFluidValid(0, new FluidStack(fluid, 1)) && tankPredicate.test(component)).mapToLong(FluidMachineComponent::getRecipeRemainingSpace).sum();
    }

    public void removeFromInputs(String tank, FluidStack stack) {
        AtomicLong toRemove = new AtomicLong(stack.getAmount());
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        this.inputs.stream().filter(component -> component.getFluid().getFluid() == stack.getFluid() && tankPredicate.test(component)).forEach(component -> {
            long maxExtract = Math.min(component.getFluid().getAmount(), toRemove.get());
            toRemove.addAndGet(-maxExtract);
            component.recipeExtract(maxExtract);
        });
    }

    public void addToOutputs(String tank, FluidStack stack) {
        AtomicLong toAdd = new AtomicLong(stack.getAmount());
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        this.outputs.stream()
                .filter(component -> component.isFluidValid(0, stack) && tankPredicate.test(component))
                .sorted(Comparator.comparingInt(component -> FluidStack.isSameFluidSameComponents(component.getFluid(), stack) ? -1 : 1))
                .forEach(component -> {
                    long maxInsert = Math.min(component.getRecipeRemainingSpace(), toAdd.get());
                    toAdd.addAndGet(-maxInsert);
                    component.recipeInsert(stack.getFluid(), maxInsert, null);
                });
    }

    /** IFluidHandler Stuff **/

    @Override
    public int getTanks() {
        return this.getComponents().size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        validateTankIndex(tank);
        return this.getComponents().get(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        validateTankIndex(tank);
        return this.getComponents().get(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        validateTankIndex(tank);
        return this.getComponents().get(tank).isFluidValid(0, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        FluidStack toFill = resource.copy();
        for(FluidMachineComponent component : this.getComponents()) {
            toFill.shrink(component.fill(toFill, action));
            if(toFill.isEmpty())
                break;
        }
        return resource.getAmount() - toFill.getAmount();
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        int toDrain = 0;
        for(FluidMachineComponent component : this.getComponents()) {
            toDrain += component.drain(resource.copyWithAmount(resource.getAmount() - toDrain), action).getAmount();
            if(toDrain == resource.getAmount())
                break;
        }
        return resource.copyWithAmount(toDrain);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        for(FluidMachineComponent component : this.getComponents()) {
            FluidStack drained = component.drain(maxDrain, action);
            if(!drained.isEmpty())
                return drained;
        }
        return FluidStack.EMPTY;
    }

    protected void validateTankIndex(int tank) {
        if (tank < 0 || tank >= this.getTanks())
            throw new RuntimeException("Tank " + tank + " not in valid range - [0," + this.getTanks() + ")");
    }
}
