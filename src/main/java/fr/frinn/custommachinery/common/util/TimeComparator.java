package fr.frinn.custommachinery.common.util;

import com.mojang.serialization.Codec;

import java.util.Arrays;

public class TimeComparator {

    public static final Codec<TimeComparator> CODEC = Codec.STRING.xmap(TimeComparator::new, TimeComparator::toString).stable();

    private MODE mode;
    private int timeToCompare;

    public TimeComparator(String s) {
        this.mode = MODE.fromPrefix(s.substring(0, 2));
        this.timeToCompare = Integer.parseInt(s.substring(2));
    }

    public String toString() {
        return this.mode.prefix + this.timeToCompare;
    }

    public boolean compare(int time) {
        switch (this.mode) {
            case UPPER:
                return time > this.timeToCompare;
            case UPPER_OR_EQUALS:
                return time >= this.timeToCompare;
            case EQUALS:
                return time == this.timeToCompare;
            case LESSER_OR_EQUALS:
                return time <= this.timeToCompare;
            case LESSER:
                return time < this.timeToCompare;
            default:
                return false;
        }
    }

    private enum MODE {
        UPPER(">>"),
        UPPER_OR_EQUALS(">="),
        EQUALS("=="),
        LESSER_OR_EQUALS("<="),
        LESSER("<<");

        private String prefix;

        MODE(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return this.prefix;
        }

        public static MODE fromPrefix(String prefix) {
            return Arrays.stream(values()).filter(mode -> mode.prefix.equals(prefix)).findFirst().orElseThrow(() -> new RuntimeException("Invalid Time Comparator prefix: " + prefix));
        }
    }
}
