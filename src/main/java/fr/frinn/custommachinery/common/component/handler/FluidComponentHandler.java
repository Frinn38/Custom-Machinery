package fr.frinn.custommachinery.common.component.handler;

import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.component.ICapabilityComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.apiimpl.component.config.RelativeSide;
import fr.frinn.custommachinery.apiimpl.component.config.SideMode;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.component.config.SidedFluidHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FluidComponentHandler extends AbstractComponentHandler<FluidMachineComponent> implements ICapabilityComponent, ISerializableComponent, ISyncableStuff {

    private final IFluidHandler generalHandler = new SidedFluidHandler(null, this);
    private final LazyOptional<IFluidHandler> capability = LazyOptional.of(() -> generalHandler);
    private final Map<Direction, IFluidHandler> sidedHandlers = Maps.newEnumMap(Direction.class);
    private final Map<Direction, LazyOptional<IFluidHandler>> sidedWrappers = Maps.newEnumMap(Direction.class);

    public FluidComponentHandler(IMachineComponentManager manager, List<FluidMachineComponent> components) {
        super(manager, components);
        components.forEach(component -> {
            component.getConfig().setCallback(this::configChanged);
            if(component.getMode().isInput())
                this.inputs.add(component);
            if(component.getMode().isOutput())
                this.outputs.add(component);
        });
        for(Direction direction : Direction.values())
            sidedWrappers.put(direction, LazyOptional.of(() -> new SidedFluidHandler(direction, this)));
    }

    private void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode) {
        if(oldMode.isNone() != newMode.isNone())
            getManager().getLevel().updateNeighborsAt(getManager().getTile().getBlockPos(), Registration.CUSTOM_MACHINE_BLOCK.get());
    }

    public IFluidHandler getFluidHandler() {
        return this.generalHandler;
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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if(side == null)
                return this.capability.cast();
            else if(getComponents().stream().anyMatch(component -> !component.getConfig().getSideMode(side).isNone()))
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
        ListTag componentsNBT = new ListTag();
        this.getComponents().forEach(component -> {
            CompoundTag componentNBT = new CompoundTag();
            component.serialize(componentNBT);
            componentNBT.putString("id", component.getId());
            componentsNBT.add(componentNBT);
        });
        nbt.put("fluids", componentsNBT);
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        if(nbt.contains("fluids", Tag.TAG_LIST)) {
            ListTag componentsNBT = nbt.getList("fluids", Tag.TAG_COMPOUND);
            componentsNBT.forEach(inbt -> {
                if(inbt instanceof CompoundTag componentNBT) {
                    if(componentNBT.contains("id", Tag.TAG_STRING)) {
                        this.getComponents().stream().filter(component -> component.getId().equals(componentNBT.getString("id"))).findFirst().ifPresent(component -> component.deserialize(componentNBT));
                    }
                }
            });
        }
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        this.getComponents().forEach(component -> component.getStuffToSync(container));
    }

    /** RECIPE STUFF **/

    private final List<FluidMachineComponent> inputs = new ArrayList<>();
    private final List<FluidMachineComponent> outputs = new ArrayList<>();

    public int getFluidAmount(String tank, Fluid fluid, @Nullable CompoundTag nbt) {
        Predicate<FluidMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getFluidStack().getTag() != null && Utils.testNBT(component.getFluidStack().getTag(), nbt));
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        return this.inputs.stream().filter(component -> component.getFluidStack().getFluid() == fluid && nbtPredicate.test(component) && tankPredicate.test(component)).mapToInt(component -> component.getFluidStack().getAmount()).sum();
    }

    public int getSpaceForFluid(String tank, Fluid fluid, @Nullable CompoundTag nbt) {
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        return this.outputs.stream().filter(component -> component.isFluidValid(new FluidStack(fluid, 1, nbt)) && tankPredicate.test(component)).mapToInt(FluidMachineComponent::getRecipeRemainingSpace).sum();
    }

    public void removeFromInputs(String tank, FluidStack stack) {
        AtomicInteger toRemove = new AtomicInteger(stack.getAmount());
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        this.inputs.stream().filter(component -> component.getFluidStack().getFluid() == stack.getFluid() && tankPredicate.test(component)).forEach(component -> {
            int maxExtract = Math.min(component.getFluidStack().getAmount(), toRemove.get());
            toRemove.addAndGet(-maxExtract);
            component.recipeExtract(maxExtract);
        });
        getManager().markDirty();
    }

    public void addToOutputs(String tank, FluidStack stack) {
        AtomicInteger toAdd = new AtomicInteger(stack.getAmount());
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        this.outputs.stream()
                .filter(component -> component.isFluidValid(stack) && tankPredicate.test(component))
                .sorted(Comparator.comparingInt(component -> component.getFluidStack().isFluidEqual(stack) ? -1 : 1))
                .forEach(component -> {
                    int maxInsert = Math.min(component.getRecipeRemainingSpace(), toAdd.get());
                    toAdd.addAndGet(-maxInsert);
                    component.recipeInsert(stack.getFluid(), maxInsert, stack.getTag());
                });
        getManager().markDirty();
    }

    /** Right click with fluid handler compat **/

    private final IFluidHandler interactionHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return FluidComponentHandler.this.generalHandler.getTanks();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidComponentHandler.this.generalHandler.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return FluidComponentHandler.this.generalHandler.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return FluidComponentHandler.this.generalHandler.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return FluidComponentHandler.this.generalHandler.fill(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack maxDrain, FluidAction action) {
            int remainingToDrain = maxDrain.getAmount();
            for (FluidMachineComponent component : FluidComponentHandler.this.getComponents().stream().sorted(Comparator.comparingInt(c -> c.getMode().isOutput() ? 1 : -1)).toList()) {
                if(!component.getFluidStack().isEmpty() && component.getFluidStack().isFluidEqual(maxDrain)) {
                    FluidStack stack = component.extract(maxDrain.getAmount(), FluidAction.SIMULATE);
                    if(stack.getAmount() >= remainingToDrain) {
                        if(action.execute()) {
                            component.extract(maxDrain.getAmount(), FluidAction.EXECUTE);
                            getManager().markDirty();
                        }
                        return maxDrain;
                    } else {
                        if(action.execute()) {
                            component.extract(stack.getAmount(), FluidAction.EXECUTE);
                            getManager().markDirty();
                        }
                        remainingToDrain -= stack.getAmount();
                    }
                }
            }
            if(remainingToDrain == maxDrain.getAmount())
                return FluidStack.EMPTY;
            else
                return new FluidStack(maxDrain.getFluid(), maxDrain.getAmount() - remainingToDrain, maxDrain.getTag());
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack toDrain = FluidStack.EMPTY;
            int remainingToDrain = maxDrain;
            for (FluidMachineComponent component : FluidComponentHandler.this.getComponents().stream().sorted(Comparator.comparingInt(c -> c.getMode().isOutput() ? 1 : -1)).toList()) {
                if(!component.getFluidStack().isEmpty() && (toDrain.isEmpty() || component.getFluidStack().isFluidEqual(toDrain))) {
                    FluidStack stack = component.extract(remainingToDrain, FluidAction.SIMULATE);
                    if(stack.getAmount() >= remainingToDrain) {
                        if(action.execute()) {
                            component.extract(remainingToDrain, FluidAction.EXECUTE);
                            getManager().markDirty();
                        }
                        return new FluidStack(stack.getFluid(), maxDrain, stack.getTag());
                    } else {
                        if(toDrain.isEmpty())
                            toDrain = stack;
                        if(action.execute()) {
                            component.extract(stack.getAmount(), FluidAction.EXECUTE);
                            getManager().markDirty();
                        }
                        remainingToDrain -= stack.getAmount();
                    }
                }
            }
            if(toDrain.isEmpty() || remainingToDrain == maxDrain)
                return FluidStack.EMPTY;
            else
                return new FluidStack(toDrain.getFluid(), maxDrain - remainingToDrain, toDrain.getTag());
        }
    };

    public IFluidHandler getInteractionHandler() {
        return this.interactionHandler;
    }
}
