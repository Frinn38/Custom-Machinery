package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.common.init.Registration;

public class TimeMachineComponent extends AbstractMachineComponent {

    public TimeMachineComponent(MachineComponentManager manager) {
        super(manager, Mode.NONE);
    }

    @Override
    public MachineComponentType<TimeMachineComponent> getType() {
        return Registration.TIME_MACHINE_COMPONENT.get();
    }

    public long getTime() {
        return this.getManager().getTile().getWorld().getDayTime();
    }
}
