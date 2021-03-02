package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.util.Utils;

import java.util.Locale;

public enum Mode {

    INPUT(true, false),
    OUTPUT(false, true),
    BOTH(true, true),
    NONE(false, false);

    public static final Codec<Mode> CODEC = Codec.STRING.comapFlatMap(Utils::decodeMode, Mode::toString).stable();

    private boolean isInput;
    private boolean isOutput;

    Mode(boolean isInput, boolean isOutput) {
        this.isInput = isInput;
        this.isOutput = isOutput;
    }

    public boolean isInput() {
        return this.isInput;
    }

    public boolean isOutput() {
        return this.isOutput;
    }

    public static Mode value(String value) {
        return valueOf(value.toUpperCase(Locale.ENGLISH));
    }
}
