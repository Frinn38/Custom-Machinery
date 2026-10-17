package fr.frinn.custommachinery.common.component.manager;

import fr.frinn.custommachinery.api.component.IComparatorInputComponent;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentId;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class DummyComponentManager extends MachineComponentManager {

    public DummyComponentManager(CustomMachineTile tile) {
        super(new ArrayList<>(), tile);
    }

    @Override
    public <C extends IMachineComponent> Map<MachineComponentType<C>, C> getComponents() {
        return Collections.emptyMap();
    }

    @Override
    public <C extends IMachineComponent> Map<MachineComponentId<C>, C> getAllComponents() {
        return Collections.emptyMap();
    }

    @Override
    public List<ISerializableComponent> getSerializableComponents() {
        return Collections.emptyList();
    }

    @Override
    public List<ITickableComponent> getTickableComponents() {
        return Collections.emptyList();
    }

    @Override
    public List<ISyncableStuff> getSyncableComponents() {
        return Collections.emptyList();
    }

    @Override
    public <C extends IMachineComponent> Optional<C> getComponent(MachineComponentType<C> type) {
        return Optional.empty();
    }

    @Override
    public <C extends IMachineComponent> Optional<IComponentHandler<C>> getComponentHandler(MachineComponentType<C> type) {
        return Optional.empty();
    }

    @Override
    public <C extends IComparatorInputComponent> Map<MachineComponentId<C>, C> getComparatorInputComponents() {
        return Collections.emptyMap();
    }

    @Override
    public <C extends IMachineComponent> boolean hasComponent(MachineComponentType<C> type) {
        return false;
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {

    }

    @Override
    public CustomMachineTile getTile() {
        return super.getTile();
    }

    @Override
    public void serverTick() {

    }

    @Override
    public void setChanged() {

    }

    @Override
    public void serialize(ValueOutput output) {

    }

    @Override
    public void deserialize(ValueInput input) {

    }
}
