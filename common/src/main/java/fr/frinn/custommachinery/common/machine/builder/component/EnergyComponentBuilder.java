package fr.frinn.custommachinery.common.machine.builder.component;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.builder.IComponentBuilderProperty;
import fr.frinn.custommachinery.api.component.builder.IMachineComponentBuilder;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.builder.IntComponentBuilderProperty;
import fr.frinn.custommachinery.impl.component.config.SideConfig;

import java.util.List;

public class EnergyComponentBuilder implements IMachineComponentBuilder<EnergyMachineComponent> {

    private IntComponentBuilderProperty capacity = new IntComponentBuilderProperty("capacity", 0);
    private IntComponentBuilderProperty maxInput = new IntComponentBuilderProperty("maxinput", 0);
    private IntComponentBuilderProperty maxOutput = new IntComponentBuilderProperty("maxoutput", 0);
    private List<IComponentBuilderProperty<?>> properties = Lists.newArrayList(this.capacity, this.maxInput, this.maxOutput);

    public EnergyComponentBuilder fromComponent(IMachineComponent component) {
        if(component instanceof EnergyMachineComponent energyComponent) {
            this.capacity.set((int)energyComponent.getCapacity());
            this.maxInput.set((int) energyComponent.getMaxInput());
            this.maxOutput.set((int) energyComponent.getMaxOutput());
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
        return new EnergyMachineComponent.Template(this.capacity.get(), this.maxInput.get(), this.maxOutput.get(), SideConfig.Template.DEFAULT_ALL_BOTH);
    }
}
