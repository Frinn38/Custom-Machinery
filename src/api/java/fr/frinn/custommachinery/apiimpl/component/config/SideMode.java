package fr.frinn.custommachinery.apiimpl.component.config;

public enum SideMode {

    INPUT(true, false),
    OUTPUT(false, true),
    BOTH(true, true),
    NONE(false, false);

    private final boolean isInput;
    private final boolean isOutput;

    SideMode(boolean isInput, boolean isOutput) {
        this.isInput = isInput;
        this.isOutput = isOutput;
    }

    public boolean isInput() {
        return this.isInput;
    }

    public boolean isOutput() {
        return this.isOutput;
    }

    public SideMode next() {
        return switch (this) {
            case INPUT -> OUTPUT;
            case OUTPUT -> BOTH;
            case BOTH -> NONE;
            case NONE -> INPUT;
        };
    }
}
