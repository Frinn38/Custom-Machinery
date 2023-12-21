package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.IComparatorInputComponent;
import fr.frinn.custommachinery.api.component.IDumpComponent;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MachineComponentManager implements IMachineComponentManager {

    private final CustomMachineTile tile;
    private final Map<MachineComponentType<?>, IMachineComponent> components;
    private final List<ISerializableComponent> serializableComponents;
    private final List<ITickableComponent> tickableComponents;
    private final List<ISyncableStuff> syncableComponents;
    private final List<IComparatorInputComponent> comparatorInputComponents;
    private final List<IDumpComponent> dumpComponents;
    private final Map<String, ISideConfigComponent> configComponents;

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
        StreamSupport.stream(Registration.MACHINE_COMPONENT_TYPE_REGISTRY.spliterator(), false).filter(type -> type.isDefaultComponent() && components.values().stream().noneMatch(component -> component.getType() == type)).forEach(type -> components.put(type, type.getDefaultComponentBuilder().apply(this)));
        this.components = Collections.unmodifiableMap(components);
        this.serializableComponents = this.components.values().stream().filter(component -> component instanceof ISerializableComponent).map(component -> (ISerializableComponent)component).toList();
        this.tickableComponents = this.components.values().stream().filter(component -> component instanceof ITickableComponent).map(component -> (ITickableComponent)component).toList();
        this.syncableComponents = this.components.values().stream().filter(component -> component instanceof ISyncableStuff).map(component -> (ISyncableStuff)component).toList();
        this.comparatorInputComponents = this.components.values().stream().filter(component -> component instanceof IComparatorInputComponent).map(component -> (IComparatorInputComponent)component).toList();
        this.dumpComponents = this.components.values().stream().filter(component -> component instanceof IDumpComponent).map(component -> (IDumpComponent)component).toList();
        this.configComponents = this.components.values().stream()
                .flatMap(component -> {
                    if(component instanceof IComponentHandler<?> handler)
                        return handler.getComponents().stream();
                    return Stream.of(component);
                })
                .filter(component -> component instanceof ISideConfigComponent)
                .map(component -> (ISideConfigComponent)component)
                .collect(Collectors.toUnmodifiableMap(component -> component.getType().getId().toString() + ":" + component.getId(), Function.identity()));
    }

    @Override
    public Map<MachineComponentType<?>, IMachineComponent> getComponents() {
        return this.components;
    }

    @Override
    public List<ISerializableComponent> getSerializableComponents() {
        return this.serializableComponents;
    }

    @Override
    public List<ITickableComponent> getTickableComponents() {
        return this.tickableComponents;
    }

    @Override
    public List<ISyncableStuff> getSyncableComponents() {
        return this.syncableComponents;
    }

    @Override
    public List<IComparatorInputComponent> getComparatorInputComponents() {
        return this.comparatorInputComponents;
    }

    @Override
    public List<IDumpComponent> getDumpComponents() {
        return this.dumpComponents;
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

    //ID is 'component_type_registry_name:id'
    public Optional<ISideConfigComponent> getConfigComponentById(String id) {
        return Optional.ofNullable(this.configComponents.get(id));
    }

    public Collection<ISideConfigComponent> getConfigComponents() {
        return this.configComponents.values();
    }

    @Override
    public CustomMachineTile getTile() {
        return this.tile;
    }

    @Override
    public Level getLevel() {
        return getTile().getLevel();
    }

    @Override
    public MinecraftServer getServer() {
        return getLevel().getServer();
    }

    public void serverTick() {
        getTickableComponents().forEach(ITickableComponent::serverTick);
    }

    public void clientTick() {
        getTickableComponents().forEach(ITickableComponent::clientTick);
    }

    public void markDirty() {
        this.getTile().setChanged();
        this.getTile().getProcessor().setMachineInventoryChanged();
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        getSerializableComponents().forEach(component -> component.serialize(nbt));
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        getSerializableComponents().forEach(component -> component.deserialize(nbt));
    }
}
