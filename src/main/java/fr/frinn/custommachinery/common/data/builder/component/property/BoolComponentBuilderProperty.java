package fr.frinn.custommachinery.common.data.builder.component.property;

import fr.frinn.custommachinery.client.screen.widget.EnumButton;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;

public class BoolComponentBuilderProperty extends AbstractComponentBuilderProperty<Boolean> {

    public BoolComponentBuilderProperty(String name, Boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.TYPE;
    }

    @Override
    public Widget getAsWidget(int x, int y, int width, int height) {
        return new EnumButton<>(
                x,
                y,
                width,
                height,
                button -> this.set(((EnumButton<Boolean>)button).getValue()),
                (button, matrix, mouseX, mouseY) -> {},
                value -> new StringTextComponent(value.toString()),
                Arrays.asList(true, false),
                get()
        );
    }
}
