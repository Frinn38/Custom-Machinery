package fr.frinn.custommachinery.api.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import net.minecraft.util.Mth;

import java.util.Locale;

public enum Operation {
    ADDITION,
    MULTIPLICATION,
    EXPONENTIAL;

    public static final NamedCodec<Operation> CODEC = NamedCodec.enumCodec(Operation.class);

    public static Operation value(String value) {
        return valueOf(value.toUpperCase(Locale.ROOT));
    }

    public double apply(double original, double modifier, int upgradeAmount, double min, double max) {
        return switch (this) {
            case ADDITION -> Mth.clamp(original + modifier * upgradeAmount, min, max);
            case MULTIPLICATION -> Mth.clamp(original * modifier * upgradeAmount, min, max);
            case EXPONENTIAL -> Mth.clamp(original * Math.pow(modifier, upgradeAmount), min, max);
        };
    }
}
