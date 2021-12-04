package fr.frinn.custommachinery.common.data.component;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.component.*;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.theoneprobe.IProbeInfoComponent;
import fr.frinn.custommachinery.common.util.CMCollectors;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MachineComponentManager implements IMachineComponentManager, INBTSerializable<CompoundNBT> {

    private final CustomMachineTile tile;
    private final List<IMachineComponent> components;
    private final List<ICapabilityComponent> capabilityComponents;
    private final List<ISerializableComponent> serializableComponents;
    private final List<ITickableComponent> tickableComponents;
    private final List<IProbeInfoComponent> probeInfoComponents;
    private final List<ISyncableStuff> syncableComponents;
    private final List<IComparatorInputComponent> comparatorInputComponents;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public MachineComponentManager(List<IMachineComponentTemplate<? extends IMachineComponent>> templates, CustomMachineTile tile) {
        this.tile = tile;
        List<IMachineComponent> components = new ArrayList<>();
        Map<MachineComponentType<?>, List<IMachineComponent>> handlers = new HashMap<>();
        templates.forEach(template -> {
            IMachineComponent component = template.build(this);
            if(component.getType().isSingle())
                components.add(component);
            else
                handlers.computeIfAbsent(component.getType(), type -> new ArrayList<>()).add(component);
        });
        handlers.forEach((type, list) -> components.add(type.getHandler(this, (List)Collections.unmodifiableList(list))));
        Registration.MACHINE_COMPONENT_TYPE_REGISTRY.get().getValues().stream().filter(type -> type.isDefaultComponent() && components.stream().noneMatch(component -> component.getType() == type)).forEach(type -> components.add(type.getDefaultComponentBuilder().apply(this)));
        this.components = Collections.unmodifiableList(components);
        this.capabilityComponents = this.components.stream().filter(component -> component instanceof ICapabilityComponent).map(component -> (ICapabilityComponent)component).collect(CMCollectors.toImmutableList());
        this.serializableComponents = this.components.stream().filter(component -> component instanceof ISerializableComponent).map(component -> (ISerializableComponent)component).collect(CMCollectors.toImmutableList());
        this.tickableComponents = this.components.stream().filter(component -> component instanceof ITickableComponent).map(component -> (ITickableComponent)component).collect(CMCollectors.toImmutableList());
        this.probeInfoComponents = this.components.stream().filter(component -> component instanceof IProbeInfoComponent).map(component -> (IProbeInfoComponent)component).collect(CMCollectors.toImmutableList());
        this.syncableComponents = this.components.stream().filter(component -> component instanceof ISyncableStuff).map(component -> (ISyncableStuff)component).collect(CMCollectors.toImmutableList());
        this.comparatorInputComponents = this.components.stream().filter(component -> component instanceof IComparatorInputComponent).map(component -> (IComparatorInputComponent)component).collect(CMCollectors.toImmutableList());
    }

    @Override
    public List<IMachineComponent> getComponents() {
        return this.components;
    }

    @Override
    public List<ICapabilityComponent> getCapabilityComponents() {
        return this.capabilityComponents;
    }

    @Override
    public List<ISerializableComponent> getSerializableComponents() {
        return this.serializableComponents;
    }

    @Override
    public List<ITickableComponent> getTickableComponents() {
        return this.tickableComponents;
    }

    public List<IProbeInfoComponent> getProbeInfoComponents() {
        return this.probeInfoComponents;
    }

    @Override
    public List<ISyncableStuff> getSyncableComponents() {
        return this.syncableComponents;
    }

    @Override
    public List<IComparatorInputComponent> getComparatorInputComponents() {
        return this.comparatorInputComponents;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IMachineComponent> Optional<T> getComponent(MachineComponentType<T> type) {
        return this.components.stream().filter(component -> component.getType() == type).map(component -> (T)component).findFirst();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IMachineComponent> Optional<IComponentHandler<T>> getComponentHandler(MachineComponentType<T> type) {
        return getComponent(type).filter(component -> component instanceof IComponentHandler).map(component -> (IComponentHandler<T>)component);
    }

    @Override
    public boolean hasComponent(MachineComponentType<?> type) {
        return this.components.stream().anyMatch(component -> component.getType() == type);
    }

    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        getSyncableComponents().forEach(syncableComponent -> syncableComponent.getStuffToSync(container));
    }

    @Override
    public CustomMachineTile getTile() {
        return this.tile;
    }

    @Override
    public World getWorld() {
        return getTile().getWorld();
    }

    @Override
    public MinecraftServer getServer() {
        return getWorld().getServer();
    }

    public void serverTick() {
        getTickableComponents().forEach(ITickableComponent::serverTick);
    }

    public void clientTick() {
        getTickableComponents().forEach(ITickableComponent::clientTick);
    }

    public void markDirty() {
        this.getTile().markDirty();
        this.getTile().craftingManager.setMachineInventoryChanged();
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
