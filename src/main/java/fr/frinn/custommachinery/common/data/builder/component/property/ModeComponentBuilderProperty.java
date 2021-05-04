package fr.frinn.custommachinery.common.data.builder.component.property;

import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

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
        TextFieldWidget widget = new TextFieldWidget(Minecraft.getInstance().fontRenderer, x, y, width, height, new StringTextComponent(getName()));
        widget.setText(this.get().toString());
        widget.setResponder(s -> this.set(IMachineComponent.Mode.value(s)));
        widget.setValidator(s -> {
            try {
                IMachineComponent.Mode.value(s);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
        return widget;
    }
}
