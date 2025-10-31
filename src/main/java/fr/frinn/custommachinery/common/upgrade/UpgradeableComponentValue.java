package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UpgradeableComponentValue implements Supplier<Double> {

    private final IMachineComponent component;
    private final String target;
    private final double defaultValue;
    private final double min;
    private final double max;
    private final Consumer<Double> onChange;

    private double value;

    public UpgradeableComponentValue(IMachineComponent component, @Nullable String target, double defaultValue, double min, double max, Consumer<Double> onChange) {
        this.component = component;
        this.target = target;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        this.value = defaultValue;
    }

    protected IMachineComponent component() {
        return this.component;
    }

    protected String target() {
        return this.target;
    }

    protected void refresh(ComponentModifier modifier, int upgradeAmount) {
        if(modifier == null || upgradeAmount == 0)
            this.value = this.defaultValue;
        else
            this.value = Mth.clamp(modifier.apply(this.defaultValue, upgradeAmount), this.min, this.max);

        //Notify the component that the value changed
        this.onChange.accept(this.value);
    }

    @Override
    public Double get() {
        return this.value;
    }
}
