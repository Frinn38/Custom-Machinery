package fr.frinn.custommachinery.common.component.handler;

import fr.frinn.custommachinery.api.component.IDumpComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.transfer.ICommonFluidHandler;
import fr.frinn.custommachinery.forge.transfer.ForgeFluidHandler;
import fr.frinn.custommachinery.impl.component.AbstractComponentHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FluidComponentHandler extends AbstractComponentHandler<FluidMachineComponent> implements ISerializableComponent, ISyncableStuff, ITickableComponent, IDumpComponent {

    private final ForgeFluidHandler handler = new ForgeFluidHandler(this);

    public FluidComponentHandler(IMachineComponentManager manager, List<FluidMachineComponent> components) {
        super(manager, components);
        components.forEach(component -> {
            component.getConfig().setCallback(this.handler::configChanged);
            if(component.getMode().isInput())
                this.inputs.add(component);
            if(component.getMode().isOutput())
                this.outputs.add(component);
        });
    }

    public ICommonFluidHandler getCommonFluidHandler() {
        return this.handler;
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
    public void onRemoved() {
        this.handler.invalidate();
    }

    @Override
    public void serverTick() {
        this.handler.tick();
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

    public long fill(FluidStack fluid, boolean simulate) {
        AtomicLong remaining = new AtomicLong(fluid.getAmount());
        this.getComponents().stream()
                .filter(component -> component.isFluidValid(fluid) && component.getRemainingSpace() > 0 && component.getMode().isInput())
                .sorted(Comparator.comparingInt(component -> FluidStack.isSameFluidSameComponents(component.getFluidStack(), fluid) ? -1 : 1))
                .forEach(component -> {
                    long toInput = Math.min(remaining.get(), component.insert(fluid.getFluid(), fluid.getAmount(), null, true));
                    if(toInput > 0) {
                        remaining.addAndGet(-toInput);
                        if (!simulate)
                            component.insert(fluid.getFluid(), toInput, null, false);
                    }
                });
        return fluid.getAmount() - remaining.get();
    }

    public FluidStack drain(FluidStack maxDrain, boolean simulate) {
        long remainingToDrain = maxDrain.getAmount();

        for (FluidMachineComponent component : this.getComponents()) {
            if(!component.getFluidStack().isEmpty() && FluidStack.isSameFluidSameComponents(component.getFluidStack(), maxDrain) && component.getMode().isOutput()) {
                FluidStack stack = component.extract(maxDrain.getAmount(), true);
                if(stack.getAmount() >= remainingToDrain) {
                    if(!simulate)
                        component.extract(remainingToDrain, false);
                    return maxDrain;
                } else {
                    if(!simulate)
                        component.extract(stack.getAmount(), false);
                    remainingToDrain -= stack.getAmount();
                }
            }
        }
        if(remainingToDrain == maxDrain.getAmount())
            return FluidStack.EMPTY;
        else
            return new FluidStack(maxDrain.getFluid(), (int)(maxDrain.getAmount() - remainingToDrain));
    }

    /** RECIPE STUFF **/

    private final List<FluidMachineComponent> inputs = new ArrayList<>();
    private final List<FluidMachineComponent> outputs = new ArrayList<>();

    public long getFluidAmount(String tank, Fluid fluid, @Nullable CompoundTag nbt) {
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        return this.inputs.stream().filter(component -> component.getFluidStack().getFluid() == fluid).mapToLong(component -> component.getFluidStack().getAmount()).sum();
    }

    public long getSpaceForFluid(String tank, Fluid fluid, @Nullable CompoundTag nbt) {
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        return this.outputs.stream().filter(component -> component.isFluidValid(new FluidStack(fluid, 1)) && tankPredicate.test(component)).mapToLong(FluidMachineComponent::getRecipeRemainingSpace).sum();
    }

    public void removeFromInputs(String tank, FluidStack stack) {
        AtomicLong toRemove = new AtomicLong(stack.getAmount());
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        this.inputs.stream().filter(component -> component.getFluidStack().getFluid() == stack.getFluid() && tankPredicate.test(component)).forEach(component -> {
            long maxExtract = Math.min(component.getFluidStack().getAmount(), toRemove.get());
            toRemove.addAndGet(-maxExtract);
            component.recipeExtract(maxExtract);
        });
    }

    public void addToOutputs(String tank, FluidStack stack) {
        AtomicLong toAdd = new AtomicLong(stack.getAmount());
        Predicate<FluidMachineComponent> tankPredicate = component -> tank.isEmpty() || component.getId().equals(tank);
        this.outputs.stream()
                .filter(component -> component.isFluidValid(stack) && tankPredicate.test(component))
                .sorted(Comparator.comparingInt(component -> FluidStack.isSameFluidSameComponents(component.getFluidStack(), stack) ? -1 : 1))
                .forEach(component -> {
                    long maxInsert = Math.min(component.getRecipeRemainingSpace(), toAdd.get());
                    toAdd.addAndGet(-maxInsert);
                    component.recipeInsert(stack.getFluid(), maxInsert, null);
                });
    }
}
