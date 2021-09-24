package fr.frinn.custommachinery.common.data.component.handler;

import fr.frinn.custommachinery.api.component.ICapabilityComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.data.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FluidComponentHandler extends AbstractComponentHandler<FluidMachineComponent> implements IFluidHandler, ICapabilityComponent, ISerializableComponent, ISyncableStuff {

    private LazyOptional<FluidComponentHandler> capability = LazyOptional.of(() -> this);

    public FluidComponentHandler(IMachineComponentManager manager) {
        super(manager);
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
    public void putComponent(FluidMachineComponent component) {
        super.putComponent(component);
        if(component.getMode().isInput())
            this.inputs.add(component);
        if(component.getMode().isOutput())
            this.outputs.add(component);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return this.capability.cast();
        return LazyOptional.empty();
    }

    @Override
    public void invalidateCapability() {
        this.capability.invalidate();
    }

    @Override
    public void serialize(CompoundNBT nbt) {
        ListNBT componentsNBT = new ListNBT();
        this.getComponents().forEach(component -> {
            CompoundNBT componentNBT = new CompoundNBT();
            component.serialize(componentNBT);
            componentsNBT.add(componentNBT);
        });
        nbt.put("fluids", componentsNBT);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        if(nbt.contains("fluids", Constants.NBT.TAG_LIST)) {
            ListNBT componentsNBT = nbt.getList("fluids", Constants.NBT.TAG_COMPOUND);
            componentsNBT.forEach(inbt -> {
                if(inbt instanceof CompoundNBT) {
                    CompoundNBT componentNBT = (CompoundNBT)inbt;
                    if(componentNBT.contains("id", Constants.NBT.TAG_STRING)) {
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

    /** FLUID HANDLER STUFF **/

    @Override
    public int getTanks() {
        return this.getComponents().size();
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int index) {
        return this.getComponents().get(index).getFluidStack();
    }

    @Override
    public int getTankCapacity(int index) {
        return this.getComponents().get(index).getCapacity();
    }

    @Override
    public boolean isFluidValid(int index, @Nonnull FluidStack stack) {
        return this.getComponents().get(index).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        AtomicInteger remaining = new AtomicInteger(stack.getAmount());
        this.getComponents().forEach(component -> {
            if(component.isFluidValid(stack) && component.getRemainingSpace() > 0 && component.getMode().isInput()) {
                int toInput = Math.min(remaining.get(), component.insert(stack.getFluid(), stack.getAmount(), FluidAction.SIMULATE));
                if(toInput > 0) {
                    remaining.addAndGet(-toInput);
                    if (action.execute()) {
                        component.insert(stack.getFluid(), toInput, FluidAction.EXECUTE);
                        getManager().markDirty();
                    }
                }
            }
        });
        return stack.getAmount() - remaining.get();
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack maxDrain, FluidAction action) {
        int remainingToDrain = maxDrain.getAmount();
        for (FluidMachineComponent component : this.getComponents()) {
            if(!component.getFluidStack().isEmpty() && component.isFluidValid(maxDrain) && component.getMode().isOutput()) {
                FluidStack stack = component.extract(maxDrain.getAmount(), FluidAction.SIMULATE);
                if(stack.getAmount() > remainingToDrain) {
                    if(action.execute()) {
                        component.extract(maxDrain.getAmount(), FluidAction.EXECUTE);
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
            return new FluidStack(maxDrain.getFluid(), maxDrain.getAmount() - remainingToDrain);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        Fluid fluid = Fluids.EMPTY;
        int remainingToDrain = maxDrain;
        for (FluidMachineComponent component : this.getComponents()) {
            if(!component.getFluidStack().isEmpty() && component.getMode().isOutput() && (fluid == Fluids.EMPTY || fluid == component.getFluidStack().getFluid())) {
                FluidStack stack = component.extract(remainingToDrain, FluidAction.SIMULATE);
                if(stack.getAmount() > remainingToDrain) {
                    if(action.execute()) {
                        component.extract(maxDrain, FluidAction.EXECUTE);
                        getManager().markDirty();
                    }
                    return new FluidStack(stack.getFluid(), maxDrain);
                } else {
                    fluid = stack.getFluid();
                    if(action.execute()) {
                        component.extract(stack.getAmount(), FluidAction.EXECUTE);
                        getManager().markDirty();
                    }
                    remainingToDrain -= stack.getAmount();
                }
            }
        }
        if(fluid == null || remainingToDrain == maxDrain)
            return FluidStack.EMPTY;
        else
            return new FluidStack(fluid, maxDrain - remainingToDrain);
    }

    /** RECIPE STUFF **/

    private List<FluidMachineComponent> inputs = new ArrayList<>();
    private List<FluidMachineComponent> outputs = new ArrayList<>();

    public int getFluidAmount(String tank, Fluid fluid, @Nullable CompoundNBT nbt) {
        Predicate<FluidMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getFluidStack().getTag() != null && Utils.testNBT(component.getFluidStack().getTag(), nbt));
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        return this.inputs.stream().filter(component -> component.getFluidStack().getFluid() == fluid && nbtPredicate.test(component) && tankPredicate.test(component)).mapToInt(component -> component.getFluidStack().getAmount()).sum();
    }

    public int getSpaceForFluid(String tank, Fluid fluid, @Nullable CompoundNBT nbt) {
        Predicate<FluidMachineComponent> nbtPredicate = component -> nbt == null || nbt.isEmpty() || (component.getFluidStack().getTag() != null && Utils.testNBT(component.getFluidStack().getTag(), nbt));
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        return this.outputs.stream().filter(component -> component.isFluidValid(new FluidStack(fluid, 1)) && nbtPredicate.test(component) && tankPredicate.test(component)).mapToInt(FluidMachineComponent::getRecipeRemainingSpace).sum();
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
        this.outputs.stream().filter(component -> component.isFluidValid(stack) && tankPredicate.test(component)).forEach(component -> {
            int maxInsert = Math.min(component.getRecipeRemainingSpace(), toAdd.get());
            toAdd.addAndGet(-maxInsert);
            component.recipeInsert(stack.getFluid(), maxInsert);
        });
        getManager().markDirty();
    }
}
