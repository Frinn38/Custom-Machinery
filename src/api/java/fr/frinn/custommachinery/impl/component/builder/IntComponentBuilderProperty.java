package fr.frinn.custommachinery.impl.component.builder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

public class IntComponentBuilderProperty extends AbstractComponentBuilderProperty<Integer> {

    public IntComponentBuilderProperty(String name, int defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public Widget getAsWidget(int x, int y, int width, int height) {
        TextFieldWidget widget = new TextFieldWidget(Minecraft.getInstance().fontRenderer, x, y, width, height, new StringTextComponent(getName())) {
            @Override
            public void writeText(String textToWrite) {
                super.writeText(textToWrite);
                while (getText().startsWith("0") && getText().length() > 1)
                    setText(getText().substring(1));
            }
        };
        widget.setText(this.get().toString());
        widget.setResponder(s -> this.set(Integer.parseInt(s)));
        widget.setValidator(s -> {
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        return widget;
    }
}
