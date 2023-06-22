package fr.frinn.custommachinery.impl.component.builder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class StringComponentBuilderProperty extends AbstractComponentBuilderProperty<String> {

    public StringComponentBuilderProperty(String name, String defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public AbstractWidget getAsWidget(int x, int y, int width, int height) {
        EditBox widget = new EditBox(Minecraft.getInstance().font, x, y, width, height, Component.literal(getName()));
        widget.setValue(this.get());
        widget.setResponder(this::set);
        return widget;
    }
}
