package fr.frinn.custommachinery.client.screen.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

public class FloatSlider extends AbstractSliderButton {
    public static Builder builder() {
        return new Builder();
    }

    private final Component baseMessage;
    private final float min;
    private final float max;
    private final boolean onlyValue;
    private final int decimalsToShow;
    private final Consumer<Float> responder;

    private FloatSlider(int x, int y, int width, int height, Component message, float value, float min, float max, boolean onlyValue, int decimalsToShow, Consumer<Float> responder) {
        super(x, y, width, height, onlyValue ? Component.literal(formatValue(Mth.map(value, 0.0F, 1.0F, min, max), decimalsToShow)) : Component.empty().append(message).append(": " + formatValue(Mth.map(value, 0.0F, 1.0F, min, max), decimalsToShow)), value);
        this.baseMessage = message;
        this.min = min;
        this.max = max;
        this.onlyValue = onlyValue;
        this.decimalsToShow = decimalsToShow;
        this.responder = responder;
    }

    public float floatValue() {
        if(this.decimalsToShow == -1)
            return (float)Mth.map(this.value, 0.0F, 1.0F, this.min, this.max);
        else
            return BigDecimal.valueOf((float)Mth.map(this.value, 0.0F, 1.0F, this.min, this.max)).setScale(this.decimalsToShow, RoundingMode.HALF_UP).floatValue();
    }

    public void setValue(float value) {
        this.value = Mth.clamp(Mth.map(value, this.min, this.max, 0.0F, 1.0F), 0, 1);
        this.applyValue();
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        if(this.onlyValue)
            this.setMessage(Component.literal(formatValue(this.floatValue(), this.decimalsToShow)));
        else
            this.setMessage(Component.empty().append(this.baseMessage).append(": " + formatValue(this.floatValue(), this.decimalsToShow)));
    }

    @Override
    protected void applyValue() {
        this.responder.accept(this.floatValue());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        float value = this.floatValue();
        boolean pressed = super.keyPressed(keyCode, scanCode, modifiers);
        float modifier = Screen.hasShiftDown() ? this.max / 10 : Screen.hasControlDown() ? this.max / 20 : this.decimalsToShow == -1 ? 0.1f : 1.0f/(float)Math.pow(10, this.decimalsToShow);
        switch(keyCode) {
            case GLFW.GLFW_KEY_RIGHT -> this.setValue(value + modifier);
            case GLFW.GLFW_KEY_LEFT -> this.setValue(value - modifier);
        }
        return pressed;
    }

    private static String formatValue(float value, int decimalsToShow) {
        if(decimalsToShow == -1)
            return String.format("%f", value);
        else
            return String.format("%1." + decimalsToShow + "f", value);
    }

    public static class Builder {

        private float defaultValue = 0;
        private float min = 0;
        private float max = 1000;
        private boolean onlyValue = false;
        private int decimalsToShow = -1;
        private Consumer<Float> responder = value -> {};

        public Builder bounds(float min, float max) {
            this.min = min;
            this.max = max;
            this.defaultValue = Mth.clamp(this.defaultValue, this.min, this.max);
            return this;
        }

        public Builder defaultValue(float defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder displayOnlyValue() {
            this.onlyValue = true;
            return this;
        }

        public Builder decimalsToShow(int decimalsToShow) {
            this.decimalsToShow = decimalsToShow;
            return this;
        }

        public Builder setResponder(Consumer<Float> responder) {
            this.responder = responder;
            return this;
        }

        public FloatSlider create(int x, int y, int width, int height, Component message) {
            return new FloatSlider(x, y, width, height, message, Mth.map(Mth.clamp(this.defaultValue, this.min, this.max), this.min, this.max, 0.0F, 1.0F), this.min, this.max, this.onlyValue, this.decimalsToShow, this.responder);
        }
    }
}
