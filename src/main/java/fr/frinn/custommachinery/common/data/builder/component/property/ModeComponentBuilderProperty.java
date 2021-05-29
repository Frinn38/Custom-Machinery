package fr.frinn.custommachinery.common.data.builder.component.property;

import fr.frinn.custommachinery.client.screen.widget.EnumButton;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;

public class ModeComponentBuilderProperty extends AbstractComponentBuilderProperty<IMachineComponent.Mode> {

    public ModeComponentBuilderProperty(String name, IMachineComponent.Mode defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Class<IMachineComponent.Mode> getType() {
        return IMachineComponent.Mode.class;
    }

    @Override
    public Widget getAsWidget(int x, int y, int width, int height) {
        return new EnumButton<>(
                x,
                y,
                width,
                height,
                button -> this.set(((EnumButton<IMachineComponent.Mode>)button).getValue()),
                (button, matrix, mouseX, mouseY) -> {},
                mode -> new StringTextComponent(mode.toString()),
                Arrays.asList(IMachineComponent.Mode.values()),
                get()
        );
    }
}
