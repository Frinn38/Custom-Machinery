package fr.frinn.custommachinery.apiimpl.component.builder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

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
        EditBox widget = new EditBox(Minecraft.getInstance().font, x, y, width, height, new TextComponent(getName()));
        widget.setValue(this.get());
        widget.setResponder(this::set);
        return widget;
    }
}
