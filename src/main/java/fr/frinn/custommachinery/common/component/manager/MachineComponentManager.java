package fr.frinn.custommachinery.common.component.manager;

import fr.frinn.custommachinery.api.component.IComparatorInputComponent;
import fr.frinn.custommachinery.api.component.IDumpComponent;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentId;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.upgrade.UpgradeableComponentValue;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.network.SyncableList;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MachineComponentManager implements IMachineComponentManager {

    private final CustomMachineTile tile;
    private final Direction facing;//Cache here to avoid calling Level#getBlockState each time.
    private final Map<MachineComponentType<?>, IMachineComponent> components;
    private final Map<MachineComponentId<?>, IMachineComponent> allComponents;
    private final List<ISerializableComponent> serializableComponents;
    private final List<ITickableComponent> tickableComponents;
    private final List<ISyncableStuff> syncableComponents;
    private final Map<MachineComponentId<?>, IComparatorInputComponent> comparatorInputComponents;
    private final List<IDumpComponent> dumpComponents;
    private final Map<String, ISideConfigComponent> configComponents;
    private final Map<MachineComponentId<?>, Map<String, UpgradeableComponentValue>> upgradeableComponentValues = new HashMap<>(); //Must be initialized before components.

    @SuppressWarnings({"unchecked", "rawtypes"})
    public MachineComponentManager(List<IMachineComponentTemplate<? extends IMachineComponent>> templates, CustomMachineTile tile) {
        this.tile = tile;
        this.facing = tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Map<MachineComponentType<?>, IMachineComponent> components = new LinkedHashMap<>();
        Map<MachineComponentType<?>, List<IMachineComponent>> handlers = new LinkedHashMap<>();
        templates.forEach(template -> {
            IMachineComponent component = template.build(this);
            if(component.getType().isSingle())
                components.put(component.getType(), component);
            else if(component instanceof ItemMachineComponent)
                handlers.computeIfAbsent(CMRegistration.ITEM_MACHINE_COMPONENT.get(), _ -> new ArrayList<>()).add(component);
            else
                handlers.computeIfAbsent(component.getType(), _ -> new ArrayList<>()).add(component);
        });
        handlers.forEach((type, list) -> components.put(type, type.getHandler(this, (List)Collections.unmodifiableList(list))));
        StreamSupport.stream(CMRegistration.MACHINE_COMPONENT_TYPE_REGISTRY.spliterator(), false).filter(type -> type.isDefaultComponent() && components.values().stream().noneMatch(component -> component.getType() == type)).forEach(type -> components.put(type, type.getDefaultComponentBuilder().apply(this)));
        this.components = Collections.unmodifiableMap(components);
        this.allComponents = this.components.values().stream().flatMap(component -> component instanceof IComponentHandler<?> handler ? Stream.concat(handler.getComponents().stream(), Stream.of(handler)) : Stream.of(component)).collect(Collectors.toMap(IMachineComponent::getId, Function.identity()));
        this.serializableComponents = this.allComponents.values().stream().filter(component -> component instanceof ISerializableComponent).map(component -> (ISerializableComponent)component).toList();
        this.tickableComponents = this.allComponents.values().stream().filter(component -> component instanceof ITickableComponent).map(component -> (ITickableComponent)component).toList();
        this.syncableComponents = this.allComponents.values().stream().filter(component -> component instanceof ISyncableStuff).map(component -> (ISyncableStuff)component).toList();
        this.comparatorInputComponents = this.allComponents.values().stream().filter(component -> component instanceof IComparatorInputComponent).map(component -> (IComparatorInputComponent)component).collect(Collectors.toMap(IMachineComponent::getId, Function.identity()));
        this.dumpComponents = this.components.values().stream().filter(component -> component instanceof IDumpComponent).map(component -> (IDumpComponent)component).toList();
        this.configComponents = this.components.values().stream()
                .flatMap(component -> {
                    if(component instanceof IComponentHandler<?> handler)
                        return handler.getComponents().stream();
                    return Stream.of(component);
                })
                .filter(component -> component instanceof ISideConfigComponent)
                .map(component -> (ISideConfigComponent)component)
                .collect(Collectors.toUnmodifiableMap(component -> component.getType().getId() + ":" + component.getId(), Function.identity()));
    }

    @Override
    public <C extends IMachineComponent> Map<MachineComponentType<C>, C> getComponents() {
        return Utils.cast(this.components);
    }

    @Override
    public <C extends IMachineComponent> Map<MachineComponentId<C>, C> getAllComponents() {
        return Utils.cast(this.allComponents);
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
    public <C extends IComparatorInputComponent> Map<MachineComponentId<C>, C> getComparatorInputComponents() {
        return Utils.cast(this.comparatorInputComponents);
    }

    @Override
    public List<IDumpComponent> getDumpComponents() {
        return this.dumpComponents;
    }

    @Override
    public <C extends IMachineComponent> Optional<C> getComponent(MachineComponentType<C> type) {
        return Optional.ofNullable(Utils.cast(this.components.get(type)));
    }

    @Override
    public <C extends IMachineComponent> Optional<C> getComponent(MachineComponentId<C> id) {
        //Special case for item components as they all have their own type but the slot gui element can't know which type it should search for.
        if(id.type() == CMRegistration.ITEM_MACHINE_COMPONENT.get())
            return this.getComponentHandler(CMRegistration.ITEM_MACHINE_COMPONENT.get()).flatMap(handler -> Utils.cast(handler.getComponentForID(id.uniqueId())));
        return Optional.ofNullable(Utils.cast(this.allComponents.get(id)));
    }

    @Override
    public <C extends IMachineComponent> Optional<IComponentHandler<C>> getComponentHandler(MachineComponentType<C> type) {
        return getComponent(type).filter(component -> component instanceof IComponentHandler).map(Utils::cast);
    }

    @Override
    public <C extends IMachineComponent> boolean hasComponent(MachineComponentType<C> type) {
        return this.components.get(type) != null;
    }

    public void getStuffToSync(SyncableList list) {
        getSyncableComponents().forEach(syncableComponent -> syncableComponent.getStuffToSync(list));
    }

    //ID is 'component_type_registry_name:id'
    @Override
    public Optional<ISideConfigComponent> getConfigComponentById(String id) {
        return Optional.ofNullable(this.configComponents.get(id));
    }

    public Collection<ISideConfigComponent> getConfigComponents() {
        return this.configComponents.values();
    }

    @Override
    public Supplier<Double> addUpgradeableComponentValue(IMachineComponent component, @Nullable String target, double defaultValue, double min, double max, Consumer<Double> onChange) {
        Map<String, UpgradeableComponentValue> map = this.upgradeableComponentValues
                .computeIfAbsent(component.getId(), _ -> new HashMap<>());
        //Standard target key in case of wrong user input like 'min_input' instead of "minInput"
        String targetId = target == null ? "" : Utils.standard(target);
        if(map.containsKey(targetId))
            throw new IllegalArgumentException("Can't add 2 upgradeable values for component type: " + component.getType().getId() + " id: " + component.getId() + " target: " + target);
        UpgradeableComponentValue upgradeableValue = new UpgradeableComponentValue(component, targetId, defaultValue, min, max, onChange);
        map.put(targetId, upgradeableValue);
        return upgradeableValue;
    }

    public Optional<UpgradeableComponentValue> getUpgradeableComponentValue(MachineComponentId<?> id, String target) {
        if(this.upgradeableComponentValues.get(id) != null)
            return Optional.ofNullable(this.upgradeableComponentValues.get(id).get(Utils.standard(target)));
        return Optional.empty();
    }

    @Override
    public CustomMachineTile getTile() {
        return this.tile;
    }

    @Override
    public Level getLevel() {
        if(this.getTile().getLevel() == null)
            throw new IllegalStateException("Level is null for custom machine: " + this.getTile().getId());
        return this.getTile().getLevel();
    }

    @Override
    public MinecraftServer getServer() {
        if(this.getLevel().getServer() == null)
            throw new IllegalStateException("Server is null for custom machine: " + this.getTile().getId());
        return this.getLevel().getServer();
    }

    @Override
    public Direction facing() {
        return this.facing;
    }

    public void serverTick() {
        this.getTickableComponents().forEach(component -> {
            if(!this.tile.isRemoved())
                component.serverTick();
        });
    }

    public void clientTick() {
        this.getTickableComponents().forEach(component -> {
            if(!this.tile.isRemoved())
                component.clientTick();
        });
    }

    public void setChanged() {
        this.getTile().setChanged();
        this.getTile().getProcessor().setMachineInventoryChanged();
    }

    public void serialize(ValueOutput output) {
        this.getSerializableComponents().forEach(component ->
            component.serialize(output.child(component.getId().toString())));
    }

    public void deserialize(ValueInput input) {
        this.getSerializableComponents().forEach(component ->
            input.child(component.getId().toString()).ifPresent(component::deserialize));
    }
}
