package fr.frinn.custommachinery.common.data.builder.component.property;

import fr.frinn.custommachinery.api.components.ComponentIOMode;
import fr.frinn.custommachinery.client.screen.widget.EnumButton;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;

public class ModeComponentBuilderProperty extends AbstractComponentBuilderProperty<ComponentIOMode> {

    public ModeComponentBuilderProperty(String name, ComponentIOMode defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Class<ComponentIOMode> getType() {
        return ComponentIOMode.class;
    }

    @Override
    public Widget getAsWidget(int x, int y, int width, int height) {
        return new EnumButton<>(
                x,
                y,
                width,
                height,
                button -> this.set(((EnumButton<ComponentIOMode>)button).getValue()),
                (button, matrix, mouseX, mouseY) -> {},
                mode -> new StringTextComponent(mode.toString()),
                Arrays.asList(ComponentIOMode.values()),
                get()
        );
    }
}
