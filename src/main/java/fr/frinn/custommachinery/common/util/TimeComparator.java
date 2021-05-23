package fr.frinn.custommachinery.common.util;

public class TimeComparator {

    private ComparatorMode mode;
    private int timeToCompare;

    public TimeComparator(String s) {
        this.mode = ComparatorMode.value(s.substring(0, 2));
        this.timeToCompare = Integer.parseInt(s.substring(2));
    }

    public String toString() {
        return this.mode.getPrefix() + this.timeToCompare;
    }

    public boolean compare(int time) {
        return this.mode.compare(time, this.timeToCompare);
    }
}
