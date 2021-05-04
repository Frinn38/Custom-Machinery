package fr.frinn.custommachinery.common.data.builder.component.property;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

public class StringComponentBuilderProperty extends AbstractComponentBuilderProperty<String> {

    public StringComponentBuilderProperty(String name, String defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public Widget getAsWidget(int x, int y, int width, int height) {
        TextFieldWidget widget = new TextFieldWidget(Minecraft.getInstance().fontRenderer, x, y, width, height, new StringTextComponent(getName()));
        widget.setText(this.get());
        widget.setResponder(this::set);
        return widget;
    }
}
