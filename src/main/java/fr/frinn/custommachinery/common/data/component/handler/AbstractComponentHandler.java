package fr.frinn.custommachinery.common.data.component.handler;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;

import java.util.List;

public abstract class AbstractComponentHandler<T extends IMachineComponent> implements IComponentHandler<T> {

    private final IMachineComponentManager manager;
    private final List<T> components;

    public AbstractComponentHandler(IMachineComponentManager manager, List<T> components) {
        this.manager = manager;
        this.components = components;
    }

    public IMachineComponentManager getManager() {
        return this.manager;
    }

    @Override
    public ComponentIOMode getMode() {
        return ComponentIOMode.NONE;
    }

    @Override
    public List<T> getComponents() {
        return this.components;
    }
}
