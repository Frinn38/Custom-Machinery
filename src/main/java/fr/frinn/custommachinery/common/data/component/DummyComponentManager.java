package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.component.*;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.integration.theoneprobe.IProbeInfoComponent;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DummyComponentManager extends MachineComponentManager {

    public DummyComponentManager(CustomMachineTile tile) {
        super(new ArrayList<>(), tile);
    }

    @Override
    public List<IMachineComponent> getComponents() {
        return Collections.emptyList();
    }

    @Override
    public List<ICapabilityComponent> getCapabilityComponents() {
        return Collections.emptyList();
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
    public List<IProbeInfoComponent> getProbeInfoComponents() {
        return Collections.emptyList();
    }

    @Override
    public List<ISyncableStuff> getSyncableComponents() {
        return Collections.emptyList();
    }

    @Override
    public <T extends IMachineComponent> Optional<T> getComponent(MachineComponentType<T> type) {
        return Optional.empty();
    }

    @Override
    public <T extends IMachineComponent> Optional<IComponentHandler<T>> getComponentHandler(MachineComponentType<T> type) {
        return Optional.empty();
    }

    @Override
    public List<IComparatorInputComponent> getComparatorInputComponents() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasComponent(MachineComponentType<?> type) {
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
    public void markDirty() {

    }

    @Override
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }
}
