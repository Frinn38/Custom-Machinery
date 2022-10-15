package fr.frinn.custommachinery.common.machine.builder.component;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.builder.IComponentBuilderProperty;
import fr.frinn.custommachinery.api.component.builder.IMachineComponentBuilder;
import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.builder.IntComponentBuilderProperty;
import fr.frinn.custommachinery.impl.component.builder.ModeComponentBuilderProperty;
import fr.frinn.custommachinery.impl.component.builder.StringComponentBuilderProperty;

import java.util.ArrayList;
import java.util.List;

public class FluidComponentBuilder implements IMachineComponentBuilder<FluidMachineComponent> {

    private StringComponentBuilderProperty id = new StringComponentBuilderProperty("id", "");
    private IntComponentBuilderProperty capacity = new IntComponentBuilderProperty("capacity", 0);
    private IntComponentBuilderProperty maxInput = new IntComponentBuilderProperty("maxinput", 0);
    private IntComponentBuilderProperty maxOutput = new IntComponentBuilderProperty("maxoutput", 0);
    private ModeComponentBuilderProperty mode = new ModeComponentBuilderProperty("mode", ComponentIOMode.BOTH);
    private List<IComponentBuilderProperty<?>> properties = Lists.newArrayList(id, capacity, maxInput, maxOutput, mode);

    public FluidComponentBuilder fromComponent(IMachineComponent component) {
        if(component instanceof FluidMachineComponent) {
            FluidMachineComponent fluidComponent = (FluidMachineComponent)component;
            this.id.set(fluidComponent.getId());
            this.capacity.set((int)fluidComponent.getCapacity());
            this.maxInput.set((int)fluidComponent.getMaxInput());
            this.maxOutput.set((int)fluidComponent.getMaxOutput());
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
        return new FluidMachineComponent.Template(id.get(), capacity.get(), maxInput.get(), maxOutput.get(), new ArrayList<>(), false, mode.get(), null);
    }
}
