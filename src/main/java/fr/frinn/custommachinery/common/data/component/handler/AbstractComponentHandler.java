package fr.frinn.custommachinery.common.data.component.handler;

import com.google.common.collect.ImmutableList;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractComponentHandler<T extends IMachineComponent> implements IComponentHandler<T> {

    private IMachineComponentManager manager;
    private List<T> components = new ArrayList<>();

    public AbstractComponentHandler(IMachineComponentManager manager) {
        this.manager = manager;
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
        return ImmutableList.copyOf(this.components);
    }

    @Override
    public void putComponent(T component) {
        this.components.add(component);
    }
}
