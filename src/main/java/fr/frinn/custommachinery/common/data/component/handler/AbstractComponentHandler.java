package fr.frinn.custommachinery.common.data.component.handler;

import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentManager;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractComponentHandler<T extends IMachineComponent> implements IComponentHandler<T> {

    private MachineComponentManager manager;
    private List<T> components = new ArrayList<>();

    public AbstractComponentHandler(MachineComponentManager manager) {
        this.manager = manager;
    }

    public MachineComponentManager getManager() {
        return this.manager;
    }

    @Override
    public List<T> getComponents() {
        return this.components;
    }

    @Override
    public void putComponent(T component) {
        this.components.add(component);
    }
}
