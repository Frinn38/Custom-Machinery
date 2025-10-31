package fr.frinn.custommachinery.impl.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractMachineComponent implements IMachineComponent {

    private final IMachineComponentManager manager;
    private final ComponentIOMode mode;

    public AbstractMachineComponent(IMachineComponentManager manager, ComponentIOMode mode) {
        this.manager = manager;
        this.mode = mode;
    }

    @Override
    public ComponentIOMode getMode() {
        return this.mode;
    }

    @Override
    public IMachineComponentManager getManager() {
        return this.manager;
    }

    protected Supplier<Double> upgradeableD(double defaultValue, String target, double min, double max, Consumer<Double> onChange) {
        return this.getManager().addUpgradeableComponentValue(this, target, defaultValue, min, max, onChange);
    }

    protected Supplier<Double> upgradeableD(double defaultValue, String target, double min, double max) {
        return this.upgradeableD(defaultValue, target, min, max, value -> {});
    }

    protected Supplier<Double> upgradeableD(double defaultValue, String target) {
        return this.upgradeableD(defaultValue, target, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    protected Supplier<Double> upgradeableD(double defaultValue) {
        return this.upgradeableD(defaultValue, "");
    }

    protected Supplier<Float> upgradeableF(double defaultValue, String target, double min, double max, Consumer<Float> onChange) {
        Supplier<Double> supplier = this.getManager().addUpgradeableComponentValue(this, target, defaultValue, min, max, value -> onChange.accept(value.floatValue()));
        return () -> supplier.get().floatValue();
    }

    protected Supplier<Float> upgradeableF(double defaultValue, String target, double min, double max) {
        return this.upgradeableF(defaultValue, target, min, max, value -> {});
    }

    protected Supplier<Float> upgradeableF(double defaultValue, String target) {
        return this.upgradeableF(defaultValue, target, Float.MIN_VALUE, Float.MAX_VALUE);
    }

    protected Supplier<Float> upgradeableF(double defaultValue) {
        return this.upgradeableF(defaultValue, "");
    }

    protected Supplier<Integer> upgradeableI(int defaultValue, String target, int min, int max, Consumer<Integer> onChange) {
        Supplier<Double> supplier = this.getManager().addUpgradeableComponentValue(this, target, defaultValue, min, max, value -> onChange.accept(value.intValue()));
        return () -> supplier.get().intValue();
    }

    protected Supplier<Integer> upgradeableI(int defaultValue, String target, int min, int max) {
        return this.upgradeableI(defaultValue, target, min, max, value -> {});
    }

    protected Supplier<Integer> upgradeableI(int defaultValue, String target) {
        return this.upgradeableI(defaultValue, target, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    protected Supplier<Integer> upgradeableI(int defaultValue) {
        return this.upgradeableI(defaultValue, "");
    }

    protected Supplier<Long> upgradeableL(long defaultValue, String target, long min, long max, Consumer<Long> onChange) {
        Supplier<Double> supplier = this.getManager().addUpgradeableComponentValue(this, target, defaultValue, min, max, value -> onChange.accept(value.longValue()));
        return () -> supplier.get().longValue();
    }

    protected Supplier<Long> upgradeableL(long defaultValue, String target, long min, long max) {
        return this.upgradeableL(defaultValue, target, min, max, value -> {});
    }

    protected Supplier<Long> upgradeableL(long defaultValue, String target) {
        return this.upgradeableL(defaultValue, target, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    protected Supplier<Long> upgradeableL(long defaultValue) {
        return this.upgradeableL(defaultValue, "");
    }
}
