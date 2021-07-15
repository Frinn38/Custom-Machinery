package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.components.ComponentIOMode;
import fr.frinn.custommachinery.api.components.IMachineComponentManager;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;

public class FuelMachineComponent extends AbstractMachineComponent {

    public FuelMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<FuelMachineComponent> getType() {
        return Registration.FUEL_MACHINE_COMPONENT.get();
    }

    public boolean isBurning() {
        return ((CustomMachineTile)getManager().getTile()).fuelManager.getFuel() > 0;
    }
}
