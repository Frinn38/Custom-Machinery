package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;

public class SkyMachineComponent extends AbstractMachineComponent {

    public SkyMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<SkyMachineComponent> getType() {
        return Registration.SKY_MACHINE_COMPONENT.get();
    }

    public boolean canSeeSky() {
        return getManager().getLevel().canSeeSky(getManager().getTile().getBlockPos().above());
    }
}
