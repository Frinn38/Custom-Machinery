package fr.frinn.custommachinery.common.data.builder.component;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.api.components.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.api.components.builder.IComponentBuilderProperty;
import fr.frinn.custommachinery.api.components.builder.IMachineComponentBuilder;
import fr.frinn.custommachinery.common.data.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.builder.IntComponentBuilderProperty;

import java.util.List;

public class EnergyComponentBuilder implements IMachineComponentBuilder<EnergyMachineComponent> {

    private IntComponentBuilderProperty capacity = new IntComponentBuilderProperty("capacity", 0);
    private IntComponentBuilderProperty maxInput = new IntComponentBuilderProperty("maxinput", 0);
    private IntComponentBuilderProperty maxOutput = new IntComponentBuilderProperty("maxoutput", 0);
    private List<IComponentBuilderProperty<?>> properties = Lists.newArrayList(this.capacity, this.maxInput, this.maxOutput);

    public EnergyComponentBuilder fromComponent(IMachineComponent component) {
        if(component instanceof EnergyMachineComponent) {
            EnergyMachineComponent energyComponent = (EnergyMachineComponent)component;
            this.capacity.set(energyComponent.getMaxEnergyStored());
            this.maxInput.set(energyComponent.getMaxInput());
            this.maxOutput.set(energyComponent.getMaxOutput());
        }
        return this;
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public List<IComponentBuilderProperty<?>> getProperties() {
        return this.properties;
    }

    @Override
    public IMachineComponentTemplate<EnergyMachineComponent> build() {
        return new EnergyMachineComponent.Template(this.capacity.get(), this.maxInput.get(), this.maxOutput.get());
    }
}
