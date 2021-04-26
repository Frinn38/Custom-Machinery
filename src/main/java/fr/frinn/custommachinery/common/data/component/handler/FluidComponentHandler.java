package fr.frinn.custommachinery.common.data.component.handler;

import fr.frinn.custommachinery.common.data.component.*;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.theoneprobe.IProbeInfoComponent;
import mcjty.theoneprobe.api.IProbeInfo;
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
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class FluidComponentHandler extends AbstractComponentHandler<FluidMachineComponent> implements IFluidHandler, ICapabilityMachineComponent, IProbeInfoComponent, IComponentSerializable {

    private List<FluidMachineComponent> components;
    private LazyOptional<FluidComponentHandler> capability = LazyOptional.of(() -> this);

    public FluidComponentHandler(MachineComponentManager manager) {
        super(manager);
        this.components = new ArrayList<>();
    }

    @Override
    public MachineComponentType getType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public List<FluidMachineComponent> getComponents() {
        return this.components;
    }

    public Optional<FluidMachineComponent> getComponentForId(String id) {
        return this.components.stream().filter(component -> component.getId().equals(id)).findFirst();
    }

    @Override
    public void putComponent(FluidMachineComponent component) {
        this.components.add(component);
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
        this.components.forEach(component -> {
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
                        this.components.stream().filter(component -> component.getId().equals(componentNBT.getString("id"))).findFirst().ifPresent(component -> component.deserialize(componentNBT));
                    }
                }
            });
        }
    }

    /** THE ONE PROBE COMPAT **/

    @Override
    public void addProbeInfo(IProbeInfo info) {
        this.components.forEach(component -> {
            FluidStack fluidStack = component.getFluidStack();
            Fluid fluid = fluidStack.getFluid();
            int color = fluid.getAttributes().getColor();
            if(fluid == Fluids.LAVA)
                color = new Color(255, 128, 0).getRGB();
            info.progress(fluidStack.getAmount(), component.getCapacity(), info.defaultProgressStyle().filledColor(color).alternateFilledColor(color).prefix(fluidStack.isEmpty() ? "" : fluidStack.getDisplayName().getString() + ": ").suffix("mB"));
        });
    }

    /** FLUID HANDLER STUFF **/

    @Override
    public int getTanks() {
        return this.components.size();
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int index) {
        return this.components.get(index).getFluidStack();
    }

    @Override
    public int getTankCapacity(int index) {
        return this.components.get(index).getCapacity();
    }

    @Override
    public boolean isFluidValid(int index, @Nonnull FluidStack stack) {
        return this.components.get(index).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        AtomicInteger remaining = new AtomicInteger(stack.getAmount());
        this.components.forEach(component -> {
            if(component.isFluidValid(stack) && component.getRemainingSpace() > 0 && component.getMode().isInput()) {
                int toInput = Math.min(remaining.get(), component.insert(stack.getFluid(), stack.getAmount(), FluidAction.SIMULATE));
                if(toInput > 0) {
                    remaining.addAndGet(-toInput);
                    if (action.execute()) {
                        component.insert(stack.getFluid(), toInput, FluidAction.EXECUTE);
                        this.getManager().markDirty();
                    }
                }
            }
        });
        return stack.getAmount() - remaining.get();
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack toExtract, FluidAction action) {
        int maxExtract = toExtract.getAmount();
        for (FluidMachineComponent component : this.components) {
            if(!component.getFluidStack().isEmpty() && component.isFluidValid(toExtract) && component.getMode().isOutput()) {
                FluidStack stack = component.extract(toExtract.getAmount(), FluidAction.SIMULATE);
                if(stack.getAmount() > maxExtract) {
                    if(action.execute()) {
                        component.extract(toExtract.getAmount(), FluidAction.EXECUTE);
                        this.getManager().markDirty();
                    }
                    return toExtract;
                } else {
                    if(action.execute()) {
                        component.extract(stack.getAmount(), FluidAction.EXECUTE);
                        this.getManager().markDirty();
                    }
                    maxExtract -= stack.getAmount();
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        Fluid fluid = Fluids.EMPTY;
        int toDrain = maxDrain;
        for (FluidMachineComponent component : this.components) {
            if(!component.getFluidStack().isEmpty() && component.getMode().isOutput() && (fluid == Fluids.EMPTY || fluid == component.getFluidStack().getFluid())) {
                FluidStack stack = component.extract(toDrain, FluidAction.SIMULATE);
                if(stack.getAmount() > toDrain) {
                    if(action.execute()) {
                        component.extract(maxDrain, FluidAction.EXECUTE);
                        this.getManager().markDirty();
                    }
                    return new FluidStack(stack.getFluid(), maxDrain);
                } else {
                    fluid = stack.getFluid();
                    if(action.execute()) {
                        component.extract(stack.getAmount(), FluidAction.EXECUTE);
                        this.getManager().markDirty();
                    }
                    toDrain -= stack.getAmount();
                }
            }
        }
        if(fluid == null || toDrain == 0)
            return FluidStack.EMPTY;
        else
            return new FluidStack(fluid, toDrain);
    }

    /** RECIPE STUFF **/

    private List<FluidMachineComponent> inputs = new ArrayList<>();
    private List<FluidMachineComponent> outputs = new ArrayList<>();

    public int getFluidAmount(Fluid fluid) {
        AtomicInteger amount = new AtomicInteger();
        this.inputs.stream().filter(component -> component.getFluidStack().getFluid() == fluid).forEach(component -> amount.addAndGet(component.getFluidStack().getAmount()));
        return amount.get();
    }

    public int getSpaceForFluid(Fluid fluid) {
        AtomicInteger space = new AtomicInteger();
        this.outputs.stream().filter(component -> (component.getFluidStack().getFluid() == fluid && component.getRemainingSpace() > 0) || component.getFluidStack().isEmpty()).forEach(component -> {
            if(component.getFluidStack().isEmpty())
                space.addAndGet(component.getCapacity());
            else
                space.addAndGet(component.getRemainingSpace());
        });
        return space.get();
    }

    public void removeFromInputs(FluidStack stack) {
        AtomicInteger toRemove = new AtomicInteger(stack.getAmount());
        this.inputs.stream().filter(component -> component.getFluidStack().getFluid() == stack.getFluid()).forEach(component -> {
            int maxExtract = Math.min(component.getFluidStack().getAmount(), toRemove.get());
            toRemove.addAndGet(-maxExtract);
            component.recipeExtract(maxExtract);
        });
        this.getManager().markDirty();
    }

    public void addToOutputs(FluidStack stack) {
        AtomicInteger toAdd = new AtomicInteger(stack.getAmount());
        this.outputs.stream().filter(component -> (component.getFluidStack().getFluid() == stack.getFluid() && component.getRemainingSpace() > 0) || component.getFluidStack().isEmpty()).forEach(component -> {
            int maxInsert = Math.min(component.getRecipeRemainingSpace(), toAdd.get());
            toAdd.addAndGet(-maxInsert);
            component.recipeInsert(stack.getFluid(), maxInsert);
        });
        this.getManager().markDirty();
    }
}
