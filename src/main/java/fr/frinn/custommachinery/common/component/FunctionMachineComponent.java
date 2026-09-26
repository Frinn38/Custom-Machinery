package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;

public class FunctionMachineComponent extends AbstractMachineComponent {

    public FunctionMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<FunctionMachineComponent> getType() {
        return CMRegistration.FUNCTION_MACHINE_COMPONENT.get();
    }
}
