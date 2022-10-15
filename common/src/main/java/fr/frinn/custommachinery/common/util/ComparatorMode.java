package fr.frinn.custommachinery.common.util;

import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

public enum ComparatorMode {

    GREATER(">>"),
    GREATER_OR_EQUALS(">="),
    EQUALS("=="),
    LESSER_OR_EQUALS("<="),
    LESSER("<<");

    private final String prefix;

    ComparatorMode(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public <T> boolean compare(T first, T second, Comparator<T> comparator) {
        switch (this) {
            case GREATER:
                return comparator.compare(first, second) > 0;
            case GREATER_OR_EQUALS:
                return comparator.compare(first, second) >= 0;
            case EQUALS:
                return comparator.compare(first, second) == 0;
            case LESSER_OR_EQUALS:
                return comparator.compare(first, second) <= 0;
            case LESSER:
                return comparator.compare(first, second) < 0;
            default:
                return false;
        }
    }

    public <T extends Comparable<T>> boolean compare(T first, T second) {
        return this.compare(first, second, Comparator.naturalOrder());
    }

    public String getTranslationKey() {
        switch (this) {
            case GREATER:
                return "custommachinery.comparator.greater";
            case GREATER_OR_EQUALS:
                return "custommachinery.comparator.greater_or_equals";
            case EQUALS:
                return "custommachinery.comparator.equals";
            case LESSER_OR_EQUALS:
                return "custommachinery.comparator.lesser_or_equals";
            case LESSER:
                return "custommachinery.comparator.lesser";
            default:
                return "";
        }
    }

    public static ComparatorMode value(String value) {
        try {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            return Stream.of(ComparatorMode.values()).filter(comparatorMode -> comparatorMode.prefix.equals(value)).findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid Comparator Mode: " + value));
        }
    }
}
