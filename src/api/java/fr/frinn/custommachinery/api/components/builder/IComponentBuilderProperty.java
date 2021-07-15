package fr.frinn.custommachinery.api.components.builder;

import net.minecraft.client.gui.widget.Widget;

public interface IComponentBuilderProperty<T> {

    Class<T> getType();

    T get();

    void set(T property);

    String getName();

    Widget getAsWidget(int x, int y, int width, int height);
}
