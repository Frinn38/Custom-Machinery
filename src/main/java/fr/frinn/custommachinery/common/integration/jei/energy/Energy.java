package fr.frinn.custommachinery.common.integration.jei.energy;

public class Energy {

    private int amount;
    private double chance;
    private boolean isPerTick;

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
