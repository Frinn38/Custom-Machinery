package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NumberAppearancePropertyBuilder<T extends Number> implements IAppearancePropertyBuilder<T> {

    private final Component title;
    private final MachineAppearanceProperty<T> type;
    private final T min;
    private final T max;

    public NumberAppearancePropertyBuilder(Component title, MachineAppearanceProperty<T> type, T min, T max) {
        this.title = title;
        this.type = type;
        this.min = min;
        this.max = max;
    }

    @Override
    public Component title() {
        return this.title;
    }

    @Override
    public MachineAppearanceProperty<T> getType() {
        return this.type;
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<T> supplier, Consumer<T> consumer) {
        double value = Mth.map(supplier.get().doubleValue(), min.doubleValue(), max.doubleValue(), 0, 1);
        return new AbstractSliderButton(x, y, width, height, Component.empty().append(title).append(": " + (int)(supplier.get().doubleValue())), value) {
            private double value() {
                return Mth.map(this.value, 0, 1, min.doubleValue(), max.doubleValue());
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Component.empty().append(title).append(": " + (int)value()));
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void applyValue() {
                consumer.accept((T)(Object) value());
            }
        };
    }
}
