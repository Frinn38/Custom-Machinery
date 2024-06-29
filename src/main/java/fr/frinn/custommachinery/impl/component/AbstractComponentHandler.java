package fr.frinn.custommachinery.impl.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;

import java.util.List;

public abstract class AbstractComponentHandler<T extends IMachineComponent> implements IComponentHandler<T>, ITickableComponent {

    private final IMachineComponentManager manager;
    private final List<T> components;
    private final List<ITickableComponent> tickables;

    public AbstractComponentHandler(IMachineComponentManager manager, List<T> components) {
        this.manager = manager;
        this.components = components;
        this.tickables = components.stream().filter(component -> component instanceof ITickableComponent).map(component -> (ITickableComponent)component).toList();
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

    @Override
    public void serverTick() {
        this.tickables.forEach(ITickableComponent::serverTick);
    }

    @Override
    public void clientTick() {
        this.tickables.forEach(ITickableComponent::clientTick);
    }
}
