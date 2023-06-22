package fr.frinn.custommachinery.impl.component.builder;

import fr.frinn.custommachinery.impl.util.EnumButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

public class BoolComponentBuilderProperty extends AbstractComponentBuilderProperty<Boolean> {

    public BoolComponentBuilderProperty(String name, Boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractWidget getAsWidget(int x, int y, int width, int height) {
        return new EnumButton<>(
                x,
                y,
                width,
                height,
                button -> this.set(((EnumButton<Boolean>)button).getValue()),
                (button, matrix, mouseX, mouseY) -> {},
                value -> Component.literal(value.toString()),
                Arrays.asList(true, false),
                get()
        );
    }
}
