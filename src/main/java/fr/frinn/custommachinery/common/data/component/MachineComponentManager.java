package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.component.ICapabilityComponent;
import fr.frinn.custommachinery.api.component.IComparatorInputComponent;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.theoneprobe.IProbeInfoComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class MachineComponentManager implements IMachineComponentManager, INBTSerializable<CompoundTag> {

    private final CustomMachineTile tile;
    private final Map<MachineComponentType<?>, IMachineComponent> components;
    private final List<ICapabilityComponent> capabilityComponents;
    private final List<ISerializableComponent> serializableComponents;
    private final List<ITickableComponent> tickableComponents;
    private final List<IProbeInfoComponent> probeInfoComponents;
    private final List<ISyncableStuff> syncableComponents;
    private final List<IComparatorInputComponent> comparatorInputComponents;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public MachineComponentManager(List<IMachineComponentTemplate<? extends IMachineComponent>> templates, CustomMachineTile tile) {
        this.tile = tile;
        Map<MachineComponentType<?>, IMachineComponent> components = new LinkedHashMap<>();
        Map<MachineComponentType<?>, List<IMachineComponent>> handlers = new LinkedHashMap<>();
        templates.forEach(template -> {
            IMachineComponent component = template.build(this);
            if(component.getType().isSingle())
                components.put(component.getType(), component);
            else
                handlers.computeIfAbsent(component.getType(), type -> new ArrayList<>()).add(component);
        });
        handlers.forEach((type, list) -> components.put(type, type.getHandler(this, (List)Collections.unmodifiableList(list))));
        Registration.MACHINE_COMPONENT_TYPE_REGISTRY.get().getValues().stream().filter(type -> type.isDefaultComponent() && components.values().stream().noneMatch(component -> component.getType() == type)).forEach(type -> components.put(type, type.getDefaultComponentBuilder().apply(this)));
        this.components = Collections.unmodifiableMap(components);
        this.capabilityComponents = this.components.values().stream().filter(component -> component instanceof ICapabilityComponent).map(component -> (ICapabilityComponent) component).toList();
        this.serializableComponents = this.components.values().stream().filter(component -> component instanceof ISerializableComponent).map(component -> (ISerializableComponent)component).toList();
        this.tickableComponents = this.components.values().stream().filter(component -> component instanceof ITickableComponent).map(component -> (ITickableComponent)component).toList();
        this.probeInfoComponents = this.components.values().stream().filter(component -> component instanceof IProbeInfoComponent).map(component -> (IProbeInfoComponent)component).toList();
        this.syncableComponents = this.components.values().stream().filter(component -> component instanceof ISyncableStuff).map(component -> (ISyncableStuff)component).toList();
        this.comparatorInputComponents = this.components.values().stream().filter(component -> component instanceof IComparatorInputComponent).map(component -> (IComparatorInputComponent)component).toList();
    }

    @Override
    public Map<MachineComponentType<?>, IMachineComponent> getComponents() {
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
        return Optional.ofNullable((T)this.components.get(type));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IMachineComponent> Optional<IComponentHandler<T>> getComponentHandler(MachineComponentType<T> type) {
        return getComponent(type).filter(component -> component instanceof IComponentHandler).map(component -> (IComponentHandler<T>)component);
    }

    @Override
    public boolean hasComponent(MachineComponentType<?> type) {
        return this.components.get(type) != null;
    }

    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        getSyncableComponents().forEach(syncableComponent -> syncableComponent.getStuffToSync(container));
    }

    @Override
    public CustomMachineTile getTile() {
        return this.tile;
    }

    @Override
    public Level getWorld() {
        return getTile().getLevel();
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
        this.getTile().setChanged();
        this.getTile().craftingManager.setMachineInventoryChanged();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        getSerializableComponents().forEach(component -> component.serialize(nbt));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getSerializableComponents().forEach(component -> component.deserialize(nbt));
    }
}
