package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.components.ComponentIOMode;
import fr.frinn.custommachinery.api.components.IMachineComponentManager;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;

public class TimeMachineComponent extends AbstractMachineComponent {

    public TimeMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<TimeMachineComponent> getType() {
        return Registration.TIME_MACHINE_COMPONENT.get();
    }

    public long getTime() {
        return this.getManager().getTile().getWorld().getDayTime();
    }
}
