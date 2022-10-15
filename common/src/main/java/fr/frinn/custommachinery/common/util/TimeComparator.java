package fr.frinn.custommachinery.common.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

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

    public Component getText() {
        return new TextComponent("Time " + new TranslatableComponent(this.mode.getTranslationKey()).getString() + " " + this.timeToCompare);
    }
}
