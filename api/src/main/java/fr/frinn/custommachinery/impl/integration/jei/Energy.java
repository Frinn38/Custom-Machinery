package fr.frinn.custommachinery.impl.integration.jei;

public class Energy {

    private final int amount;
    private final double chance;
    private final boolean isPerTick;

    public Energy(int amount, double chance, boolean isPerTick) {
        this.amount = amount;
        this.chance = chance;
        this.isPerTick = isPerTick;
    }

    public int getAmount() {
        return this.amount;
    }

    public double getChance() {
        return this.chance;
    }

    public boolean isPerTick() {
        return this.isPerTick;
    }
}
