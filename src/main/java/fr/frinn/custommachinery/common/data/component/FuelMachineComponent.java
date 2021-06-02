package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.common.init.Registration;

public class FuelMachineComponent extends AbstractMachineComponent {

    public FuelMachineComponent(MachineComponentManager manager) {
        super(manager, Mode.NONE);
    }

    @Override
    public MachineComponentType<FuelMachineComponent> getType() {
        return Registration.FUEL_MACHINE_COMPONENT.get();
    }

    public boolean isBurning() {
        return getManager().getTile().fuelManager.getFuel() > 0;
    }
}
