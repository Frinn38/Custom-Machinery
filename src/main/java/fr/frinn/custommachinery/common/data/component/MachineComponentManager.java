package fr.frinn.custommachinery.common.data.component;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.frinn.custommachinery.common.data.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.data.component.handler.IComponentHandler;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MachineComponentManager {

    private List<IMachineComponent> components;
    private CustomMachineTile tile;

    public MachineComponentManager(List<IMachineComponentTemplate<? extends IMachineComponent>> templates, CustomMachineTile tile) {
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
        this.tile = tile;
    }

    public List<IMachineComponent> getComponents() {
        return Lists.newArrayList(this.components);
    }

    public List<ICapabilityMachineComponent> getCapabilityComponents() {
        return this.components.stream().filter(component -> component instanceof ICapabilityMachineComponent).map(component -> (ICapabilityMachineComponent)component).collect(Collectors.toList());
    }

    public <T extends IMachineComponent> Optional<T> getComponent(MachineComponentType type) {
        return this.components.stream().filter(component -> component.getType() == type).map(component -> (T)component).findFirst();
    }

    public <T extends IMachineComponent> Optional<T> getComponent(Class<T> type) {
        return this.components.stream().filter(component -> component.getClass() == type).map(component -> (T)component).findFirst();
    }

    public boolean hasComponent(MachineComponentType type) {
        return this.components.stream().anyMatch(component -> component.getType() == type);
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

    public void markDirty() {
        this.tile.markForSyncing();
    }

}
