package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class NumberAppearancePropertyBuilder<T extends Number> implements IAppearancePropertyBuilder<T> {

    private final Component title;
    private final MachineAppearanceProperty<T> type;
    private final T min;
    private final T max;
    @Nullable
    private final Component tooltip;

    public NumberAppearancePropertyBuilder(Component title, MachineAppearanceProperty<T> type, T min, T max, @Nullable Component tooltip) {
        this.title = title;
        this.type = type;
        this.min = min;
        this.max = max;
        this.tooltip = tooltip;
    }

    public abstract T fromDouble(double value);

    @Override
    public Component title() {
        return this.title;
    }

    @Override
    public MachineAppearanceProperty<T> type() {
        return this.type;
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<T> supplier, Consumer<T> consumer) {
        double value = Mth.map(supplier.get().doubleValue(), min.doubleValue(), max.doubleValue(), 0, 1);
        AbstractSliderButton button = new AbstractSliderButton(x, y, width, height, Component.empty().append(title).append(": " + (int)(supplier.get().doubleValue())), value) {
            private double value() {
                return Mth.map(this.value, 0, 1, min.doubleValue(), max.doubleValue());
            }

            private void setValue(double value) {
                this.value = Mth.clamp(Mth.map(value, min.doubleValue(), max.doubleValue(), 0, 1), 0, 1);
                this.applyValue();
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Component.empty().append(title).append(": " + (int)value()));
            }

            @Override
            protected void applyValue() {
                consumer.accept(fromDouble(value()));
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                double value = this.value();
                boolean pressed = super.keyPressed(keyCode, scanCode, modifiers);
                double modifier = Screen.hasShiftDown() ? max.doubleValue() / 10 : Screen.hasControlDown() ? max.doubleValue() / 20 : 1;
                switch(keyCode) {
                    case GLFW.GLFW_KEY_RIGHT -> this.setValue(value + modifier);
                    case GLFW.GLFW_KEY_LEFT -> this.setValue(value - modifier);
                }
                return pressed;
            }
        };
        if(this.tooltip != null)
            button.setTooltip(Tooltip.create(this.tooltip));
        return button;
    }

    public static class IntegerAppearancePropertyBuilder extends NumberAppearancePropertyBuilder<Integer> {

        public IntegerAppearancePropertyBuilder(Component title, MachineAppearanceProperty<Integer> type, Integer min, Integer max, @Nullable Component tooltip) {
            super(title, type, min, max, tooltip);
        }

        @Override
        public Integer fromDouble(double value) {
            return (int)value;
        }
    }

    public static class FloatAppearancePropertyBuilder extends NumberAppearancePropertyBuilder<Float> {

        public FloatAppearancePropertyBuilder(Component title, MachineAppearanceProperty<Float> type, Float min, Float max, @Nullable Component tooltip) {
            super(title, type, min, max, tooltip);
        }

        @Override
        public Float fromDouble(double value) {
            return (float)value;
        }
    }
}
