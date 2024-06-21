package fr.frinn.custommachinery.client.screen.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class IntegerSlider extends AbstractSliderButton {

    public static Builder builder() {
        return new Builder();
    }

    private final Component baseMessage;
    private final int min;
    private final int max;
    private final boolean onlyValue;
    private final Consumer<Integer> responder;

    private IntegerSlider(int x, int y, int width, int height, Component message, double value, int min, int max, boolean onlyValue, Consumer<Integer> responder) {
        super(x, y, width, height, onlyValue ? Component.literal("" + (int)Mth.map(value, 0.0D, 1.0D, min, max)) : Component.empty().append(message).append(": " + (int)Mth.map(value, 0.0D, 1.0D, min, max)), value);
        this.baseMessage = message;
        this.min = min;
        this.max = max;
        this.onlyValue = onlyValue;
        this.responder = responder;
    }

    public int intValue() {
        return (int)Mth.map(this.value, 0.0D, 1.0D, this.min, this.max);
    }

    public void setValue(int value) {
        this.value = Mth.map(value, this.min, this.max, 0.0D, 1.0D);
        this.applyValue();
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        if(this.onlyValue)
            this.setMessage(Component.literal("" + this.intValue()));
        else
            this.setMessage(Component.empty().append(this.baseMessage).append(": " + this.intValue()));
    }

    @Override
    protected void applyValue() {
        this.responder.accept(this.intValue());
    }

    public static class Builder {

        private int defaultValue = 0;
        private int min = 0;
        private int max = 1000;
        private boolean onlyValue = false;
        private Consumer<Integer> responder = value -> {};

        public Builder bounds(int min, int max) {
            this.min = min;
            this.max = max;
            this.defaultValue = Mth.clamp(this.defaultValue, this.min, this.max);
            return this;
        }

        public Builder defaultValue(int defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder displayOnlyValue() {
            this.onlyValue = true;
            return this;
        }

        public Builder setResponder(Consumer<Integer> responder) {
            this.responder = responder;
            return this;
        }

        public IntegerSlider create(int x, int y, int width, int height, Component message) {
            return new IntegerSlider(x, y, width, height, message, Mth.map(Mth.clamp(this.defaultValue, this.min, this.max), this.min, this.max, 0.0D, 1.0D), this.min, this.max, this.onlyValue, this.responder);
        }
    }
}
