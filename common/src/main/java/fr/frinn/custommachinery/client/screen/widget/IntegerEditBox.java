package fr.frinn.custommachinery.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

public class IntegerEditBox extends EditBox {

    private int max = Integer.MAX_VALUE;
    private int min = Integer.MIN_VALUE;

    public IntegerEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        this.setFilter(s -> true);
    }

    public void bounds(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getIntValue() {
        if(this.getValue().isEmpty())
            return 0;
        try {
            return Integer.parseInt(this.getValue());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void setFilter(Predicate<String> validator) {
        super.setFilter(s -> {
            if(s.isEmpty()) {
                this.setValue("0");
                return true;
            }
            try {
                int value = Integer.parseInt(s);
                return value >= this.min && value <= this.max && validator.test(s);
            } catch (NumberFormatException e) {
                return false;
            }
        });
    }
}
