package fr.frinn.custommachinery.client.screen.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class DoubleSlider extends AbstractSliderButton {

    public static Builder builder() {
        return new Builder();
    }

    private final Component baseMessage;
    private final double min;
    private final double max;
    private final boolean onlyValue;
    private final Consumer<Double> responder;

    private DoubleSlider(int x, int y, int width, int height, Component message, double value, double min, double max, boolean onlyValue, Consumer<Double> responder) {
        super(x, y, width, height, onlyValue ? Component.literal(String.format("%.2f", Mth.map(value, 0.0D, 1.0D, min, max))) : Component.empty().append(message).append(": " + (int)Mth.map(value, 0.0D, 1.0D, min, max)), value);
        this.baseMessage = message;
        this.min = min;
        this.max = max;
        this.onlyValue = onlyValue;
        this.responder = responder;
    }

    public double doubleValue() {
        return Mth.map(this.value, 0.0D, 1.0D, this.min, this.max);
    }

    public void setValue(double value) {
        this.value = Mth.map(value, this.min, this.max, 0.0D, 1.0D);
        this.applyValue();
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        if(this.onlyValue)
            this.setMessage(Component.literal(String.format("%.2f", this.doubleValue())));
        else
            this.setMessage(Component.empty().append(this.baseMessage).append(String.format(": %.2f", this.doubleValue())));
    }

    @Override
    protected void applyValue() {
        this.responder.accept(this.doubleValue());
    }

    public static class Builder {

        private double defaultValue = 0;
        private double min = 0;
        private double max = 1000;
        private boolean onlyValue = false;
        private Consumer<Double> responder = value -> {};

        public Builder bounds(double min, double max) {
            this.min = min;
            this.max = max;
            this.defaultValue = Mth.clamp(this.defaultValue, this.min, this.max);
            return this;
        }

        public Builder defaultValue(double defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder displayOnlyValue() {
            this.onlyValue = true;
            return this;
        }

        public Builder setResponder(Consumer<Double> responder) {
            this.responder = responder;
            return this;
        }

        public DoubleSlider create(int x, int y, int width, int height, Component message) {
            return new DoubleSlider(x, y, width, height, message, Mth.map(Mth.clamp(this.defaultValue, this.min, this.max), this.min, this.max, 0.0D, 1.0D), this.min, this.max, this.onlyValue, this.responder);
        }
    }
}
