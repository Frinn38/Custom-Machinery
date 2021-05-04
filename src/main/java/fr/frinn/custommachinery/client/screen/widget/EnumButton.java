package fr.frinn.custommachinery.client.screen.widget;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.function.Function;

public class EnumButton<E> extends Button {

    private Function<E, ITextComponent> messageFunction;
    private List<E> values;
    private E value;

    public EnumButton(int x, int y, int width, int height, IPressable pressedAction, ITooltip onTooltip, Function<E, ITextComponent> messageFunction, List<E> values, E defaultValue) {
        super(x, y, width, height, StringTextComponent.EMPTY, pressedAction, onTooltip);
        this.messageFunction = messageFunction;
        this.values = values;
        if(this.values.contains(defaultValue))
            this.value = defaultValue;
        else
            this.value = this.values.get(0);
    }

    @Override
    public void onPress() {
        this.nextValue();
        super.onPress();
    }

    @Override
    public ITextComponent getMessage() {
        return this.messageFunction.apply(this.value);
    }

    public E getValue() {
        return this.value;
    }

    private void nextValue() {
        int index = this.values.indexOf(this.value);
        if(index < this.values.size() - 1)
            index++;
        else
            index = 0;
        this.value = this.values.get(index);
    }
}
