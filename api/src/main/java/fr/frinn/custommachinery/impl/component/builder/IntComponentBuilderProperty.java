package fr.frinn.custommachinery.impl.component.builder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

public class IntComponentBuilderProperty extends AbstractComponentBuilderProperty<Integer> {

    public IntComponentBuilderProperty(String name, int defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public AbstractWidget getAsWidget(int x, int y, int width, int height) {
        EditBox widget = new EditBox(Minecraft.getInstance().font, x, y, width, height, new TextComponent(getName())) {
            @Override
            public void setValue(String textToWrite) {
                super.setValue(textToWrite);
                while (getValue().startsWith("0") && getValue().length() > 1)
                    setValue(getValue().substring(1));
            }
        };
        widget.setValue(this.get().toString());
        widget.setResponder(s -> this.set(Integer.parseInt(s)));
        widget.setFilter(s -> {
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
