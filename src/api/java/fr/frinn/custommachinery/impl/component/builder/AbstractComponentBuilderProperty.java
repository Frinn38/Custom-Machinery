package fr.frinn.custommachinery.impl.component.builder;

import fr.frinn.custommachinery.api.components.builder.IComponentBuilderProperty;

public abstract class AbstractComponentBuilderProperty<T> implements IComponentBuilderProperty<T> {

    private String name;
    private T property;

    public AbstractComponentBuilderProperty(String name, T defaultValue) {
        this.name = name;
        this.property = defaultValue;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public T get() {
        return this.property;
    }

    @Override
    public void set(T property) {
        this.property = property;
    }
}
