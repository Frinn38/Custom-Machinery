package fr.frinn.custommachinery.impl.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;

public abstract class AbstractMachineComponent implements IMachineComponent {

    private IMachineComponentManager manager;
    private ComponentIOMode mode;

    public AbstractMachineComponent(IMachineComponentManager manager, ComponentIOMode mode) {
        this.manager = manager;
        this.mode = mode;
    }

    @Override
    public ComponentIOMode getMode() {
        return this.mode;
    }

    @Override
    public IMachineComponentManager getManager() {
        return this.manager;
    }
}
