package fr.frinn.custommachinery.api.component;

import java.util.Locale;

public enum ComponentIOMode {

    INPUT(true, false),
    OUTPUT(false, true),
    BOTH(true, true),
    NONE(false, false);

    private final boolean isInput;
    private final boolean isOutput;

    ComponentIOMode(boolean isInput, boolean isOutput) {
        this.isInput = isInput;
        this.isOutput = isOutput;
    }

    public boolean isInput() {
        return this.isInput;
    }

    public boolean isOutput() {
        return this.isOutput;
    }

    public static ComponentIOMode value(String value) {
        return ComponentIOMode.valueOf(value.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH);
    }
}
