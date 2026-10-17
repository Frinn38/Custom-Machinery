package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentId;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.impl.network.SyncableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DataMachineComponent extends AbstractMachineComponent implements ISerializableComponent, ISyncableStuff {

    private CompoundTag nbt = new CompoundTag();

    public DataMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    public CompoundTag getData() {
        return this.nbt;
    }

    public void setData(CompoundTag nbt) {
        this.nbt = nbt;
    }

    @Override
    public MachineComponentType<DataMachineComponent> getType() {
        return CMRegistration.DATA_MACHINE_COMPONENT.get();
    }

    @Override
    public MachineComponentId<DataMachineComponent> getId() {
        return this.getType().componentId("");
    }

    @Override
    public void serialize(ValueOutput output) {
        output.store("data_component", CompoundTag.CODEC, this.nbt);
    }

    @Override
    public void deserialize(ValueInput input) {
        input.read("data_component", CompoundTag.CODEC).ifPresent(this::setData);
    }

    @Override
    public void getStuffToSync(SyncableList list) {
        list.syncNBT(this::getData, this::setData);
    }
}
