package fr.frinn.custommachinery.common.data.component;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.common.data.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.data.component.handler.IComponentHandler;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.theoneprobe.IProbeInfoComponent;
import fr.frinn.custommachinery.common.network.sync.ISyncable;
import fr.frinn.custommachinery.common.network.sync.ISyncableStuff;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MachineComponentManager implements INBTSerializable<CompoundNBT> {

    private List<IMachineComponent> components;
    private CustomMachineTile tile;

    public MachineComponentManager(List<IMachineComponentTemplate<? extends IMachineComponent>> templates, CustomMachineTile tile) {
        this.tile = tile;
        this.components = new ArrayList<>();
        templates.forEach(template -> {
            IMachineComponent component = template.build(this);
            if(component.getType().isSingle())
                this.components.add(component);
            else {
                IComponentHandler handler = this.components.stream().filter(c -> c instanceof IComponentHandler && c.getType() == component.getType()).map(c -> (IComponentHandler)c).findFirst().orElse(null);
                if(handler != null) {
                    handler.putComponent(component);
                } else {
                    handler = component.getType().getHandler(this);
                    handler.putComponent(component);
                    this.components.add(handler);
                }
            }
        });
        Registration.MACHINE_COMPONENT_TYPE_REGISTRY.get().getValues().stream().filter(MachineComponentType::isDefaultComponent).forEach(ACTION -> this.components.add(ACTION.getDefaultComponentBuilder().apply(this)));
    }

    public List<IMachineComponent> getComponents() {
        return Lists.newArrayList(this.components);
    }

    public List<ICapabilityMachineComponent> getCapabilityComponents() {
        return this.components.stream().filter(component -> component instanceof ICapabilityMachineComponent).map(component -> (ICapabilityMachineComponent)component).collect(Collectors.toList());
    }

    public List<IComponentSerializable> getSerializableComponents() {
        return this.components.stream().filter(component -> component instanceof IComponentSerializable).map(component -> (IComponentSerializable)component).collect(Collectors.toList());
    }

    public List<ITickableMachineComponent> getTickableComponents() {
        return this.components.stream().filter(component -> component instanceof ITickableMachineComponent).map(component -> (ITickableMachineComponent)component).collect(Collectors.toList());
    }

    public List<IProbeInfoComponent> getProbeInfoComponents() {
        return this.components.stream().filter(component -> component instanceof IProbeInfoComponent).map(component -> (IProbeInfoComponent)component).collect(Collectors.toList());
    }

    public List<ISyncableStuff> getSyncableComponents() {
        return this.components.stream().filter(component -> component instanceof ISyncableStuff).map(component -> (ISyncableStuff)component).collect(Collectors.toList());
    }

    public IMachineComponent getComponentRaw(MachineComponentType ACTION) {
        return this.components.stream().filter(component -> component.getType() == ACTION).findFirst().get();
    }

    public <T extends IMachineComponent> Optional<T> getComponent(MachineComponentType ACTION) {
        return this.components.stream().filter(component -> component.getType() == ACTION).map(component -> (T)component).findFirst();
    }

    public <T extends IMachineComponent> Optional<T> getOptionalComponent(MachineComponentType<T> ACTION) {
        return this.components.stream().filter(component -> component.getType() == ACTION).map(component -> (T)component).findFirst();
    }

    public List<IComparatorInputComponent> getComparatorInputComponents() {
        return this.components.stream().filter(component -> component instanceof IComparatorInputComponent).map(component -> (IComparatorInputComponent)component).collect(Collectors.toList());
    }

    public boolean hasComponent(MachineComponentType ACTION) {
        return this.components.stream().anyMatch(component -> component.getType() == ACTION);
    }

    public Optional<EnergyMachineComponent> getEnergy() {
        return getComponent(Registration.ENERGY_MACHINE_COMPONENT.get());
    }

    public Optional<FluidComponentHandler> getFluidHandler() {
        return getComponent(Registration.FLUID_MACHINE_COMPONENT.get());
    }

    public Optional<ItemComponentHandler> getItemHandler() {
        return getComponent(Registration.ITEM_MACHINE_COMPONENT.get());
    }

    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        getSyncableComponents().forEach(syncableComponent -> syncableComponent.getStuffToSync(container));
    }

    public CustomMachineTile getTile() {
        return this.tile;
    }

    public void tick() {
        if(this.tile.isPaused())
            return;
        getTickableComponents().forEach(ITickableMachineComponent::tick);
        getEnergy().ifPresent(energyComponent -> {
            for (Direction direction : Direction.values()) {
                TileEntity tile = this.tile.getWorld().getTileEntity(this.tile.getPos().offset(direction));
                if(tile != null) {
                    tile.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite()).ifPresent(energy -> {
                        int maxOutput = energyComponent.extractEnergy(Integer.MAX_VALUE, true);
                        if(maxOutput > 0) {
                            int toOutput = energy.receiveEnergy(maxOutput, true);
                            if(toOutput > 0) {
                                energyComponent.extractEnergy(toOutput, false);
                                energy.receiveEnergy(toOutput, false);
                            }
                        }
                    });
                }
            }
        });
    }

    public void markDirty() {
        this.getTile().markDirty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        getSerializableComponents().forEach(component -> component.serialize(nbt));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        getSerializableComponents().forEach(component -> component.deserialize(nbt));
    }
}
