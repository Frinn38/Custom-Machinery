package fr.frinn.custommachinery.common.util;

import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;

import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

public enum ComparatorMode {

    GREATER(">>"),
    GREATER_OR_EQUALS(">="),
    EQUALS("=="),
    LESSER_OR_EQUALS("<="),
    LESSER("<<");

    public static final NamedCodec<ComparatorMode> CODEC = NamedCodec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(value(s));
        } catch (IllegalArgumentException e) {
            return DataResult.error(e.getMessage());
        }
    }, ComparatorMode::getPrefix, "Comparator mode");

    private final String prefix;

    ComparatorMode(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public <T> boolean compare(T first, T second, Comparator<T> comparator) {
        return switch (this) {
            case GREATER -> comparator.compare(first, second) > 0;
            case GREATER_OR_EQUALS -> comparator.compare(first, second) >= 0;
            case EQUALS -> comparator.compare(first, second) == 0;
            case LESSER_OR_EQUALS -> comparator.compare(first, second) <= 0;
            case LESSER -> comparator.compare(first, second) < 0;
        };
    }

    public <T extends Comparable<T>> boolean compare(T first, T second) {
        return this.compare(first, second, Comparator.naturalOrder());
    }

    public String getTranslationKey() {
        return switch (this) {
            case GREATER -> "custommachinery.comparator.greater";
            case GREATER_OR_EQUALS -> "custommachinery.comparator.greater_or_equals";
            case EQUALS -> "custommachinery.comparator.equals";
            case LESSER_OR_EQUALS -> "custommachinery.comparator.lesser_or_equals";
            case LESSER -> "custommachinery.comparator.lesser";
        };
    }

    public static ComparatorMode value(String value) {
        try {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            return Stream.of(ComparatorMode.values()).filter(comparatorMode -> comparatorMode.prefix.equals(value)).findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid Comparator Mode: " + value));
        }
    }
}
