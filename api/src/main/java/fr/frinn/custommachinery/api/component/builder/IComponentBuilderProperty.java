package fr.frinn.custommachinery.api.component.builder;

import net.minecraft.client.gui.components.AbstractWidget;

public interface IComponentBuilderProperty<T> {

    Class<T> getType();

    T get();

    void set(T property);

    String getName();

    AbstractWidget getAsWidget(int x, int y, int width, int height);
}
