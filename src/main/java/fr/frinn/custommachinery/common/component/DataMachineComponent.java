package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Consumer;

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
    public void serialize(ValueOutput output) {
        output.store("data_component", CompoundTag.CODEC, this.nbt);
    }

    @Override
    public void deserialize(ValueInput input) {
        input.read("data_component", CompoundTag.CODEC).ifPresent(this::setData);
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        container.accept(DataType.createSyncable(CompoundTag.class, this::getData, this::setData));
    }
}
