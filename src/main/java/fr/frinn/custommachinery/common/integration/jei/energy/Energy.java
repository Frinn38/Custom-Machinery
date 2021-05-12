package fr.frinn.custommachinery.common.integration.jei.energy;

public class Energy {

    private int amount;
    private boolean isPerTick;

    public Energy(int amount, boolean isPerTick) {
        this.amount = amount;
        this.isPerTick = isPerTick;
    }

    public Energy(int amount) {
        this(amount, false);
    }

    public int getAmount() {
        return this.amount;
    }

    public boolean isPerTick() {
        return this.isPerTick;
    }
}
