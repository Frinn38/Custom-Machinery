package fr.frinn.custommachinery.common.data.builder.component;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.common.data.builder.component.property.IComponentBuilderProperty;
import fr.frinn.custommachinery.common.data.builder.component.property.IntComponentBuilderProperty;
import fr.frinn.custommachinery.common.data.builder.component.property.ModeComponentBuilderProperty;
import fr.frinn.custommachinery.common.data.builder.component.property.StringComponentBuilderProperty;
import fr.frinn.custommachinery.common.data.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;

import java.util.ArrayList;
import java.util.List;

public class FluidComponentBuilder implements IMachineComponentBuilder<FluidMachineComponent> {

    private StringComponentBuilderProperty id = new StringComponentBuilderProperty("id", "");
    private IntComponentBuilderProperty capacity = new IntComponentBuilderProperty("capacity", 0);
    private IntComponentBuilderProperty maxInput = new IntComponentBuilderProperty("maxinput", 0);
    private IntComponentBuilderProperty maxOutput = new IntComponentBuilderProperty("maxoutput", 0);
    private ModeComponentBuilderProperty mode = new ModeComponentBuilderProperty("mode", IMachineComponent.Mode.BOTH);
    private List<IComponentBuilderProperty<?>> properties = Lists.newArrayList(id, capacity, maxInput, maxOutput, mode);

    public FluidComponentBuilder fromComponent(IMachineComponent component) {
        if(component instanceof FluidMachineComponent) {
            FluidMachineComponent fluidComponent = (FluidMachineComponent)component;
            this.id.set(fluidComponent.getId());
            this.capacity.set(fluidComponent.getCapacity());
            this.maxInput.set(fluidComponent.getMaxInput());
            this.maxOutput.set(fluidComponent.getMaxOutput());
            this.mode.set(fluidComponent.getMode());
        }
        return this;
    }

    @Override
    public MachineComponentType<FluidMachineComponent> getType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public List<IComponentBuilderProperty<?>> getProperties() {
        return this.properties;
    }

    @Override
    public IMachineComponentTemplate<FluidMachineComponent> build() {
        return new FluidMachineComponent.Template(id.get(), capacity.get(), maxInput.get(), maxOutput.get(), new ArrayList<>(), mode.get());
    }
}
