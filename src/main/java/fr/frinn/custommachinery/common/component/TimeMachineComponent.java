package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;

public class TimeMachineComponent extends AbstractMachineComponent {

    public TimeMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<TimeMachineComponent> getType() {
        return CMRegistration.TIME_MACHINE_COMPONENT.get();
    }

    public long getTime() {
        if(getManager().getLevel().dimensionType().hasFixedTime())
            return getManager().getLevel().getDefaultClockTime();//TODO: Check if time is correct here
        return getManager().getLevel().getDefaultClockTime() % 24000L;
    }
}
