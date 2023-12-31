package fr.frinn.custommachinery.impl.integration.jei;

public class ExperienceStorage implements IExperienceStorage {
  private float xp;
  private final float capacity, maxIn, maxOut;
  private final double chance;
  private final boolean isPerTick;

  public ExperienceStorage(float xp, float capacity, float maxIn, float maxOut, double chance, boolean isPerTick) {
    this.xp = xp;
    this.capacity = capacity;
    this.maxIn = maxIn;
    this.maxOut = maxOut;
    this.chance = chance;
    this.isPerTick = isPerTick;
  }

  public ExperienceStorage(float xp, float capacity, float maxIn, float maxOut) {
    this(xp, capacity, maxIn, maxOut, 1D, false);
  }

  public ExperienceStorage(float xp, float capacity) {
    this(xp, capacity, capacity, capacity, 1D, false);
  }

  public ExperienceStorage(float xp) {
    this(xp, xp, xp, xp, 1D, false);
  }



  public ExperienceStorage(float xp, float capacity, double chance, boolean isPerTick) {
    this(xp, capacity, capacity, capacity, chance, isPerTick);
  }

  public ExperienceStorage(float xp, double chance, boolean isPerTick) {
    this(xp, xp, xp, xp, chance, isPerTick);
  }

  public ExperienceStorage(float xp, float capacity, float maxIn, float maxOut, double chance) {
    this(xp, capacity, maxIn, maxOut, chance, false);
  }

  public ExperienceStorage(float xp, float capacity, double chance) {
    this(xp, capacity, capacity, capacity, chance, false);
  }

  public ExperienceStorage(float xp, double chance) {
    this(xp, xp, xp, xp, chance, false);
  }

  public ExperienceStorage(float xp, float capacity, float maxIn, float maxOut, boolean isPerTick) {
    this(xp, capacity, maxIn, maxOut, 1D, isPerTick);
  }

  public ExperienceStorage(float xp, float capacity, boolean isPerTick) {
    this(xp, capacity, capacity, capacity, 1D, isPerTick);
  }

  public ExperienceStorage(float xp, boolean isPerTick) {
    this(xp, xp, xp, xp, 1D, isPerTick);
  }

  public float getXp() {
    return xp;
  }

  public void setXp(float xp) {
    this.xp = xp;
  }

  public void addXp(float xp) {
    this.xp += xp;
  }

  public float getCapacity() {
    return capacity;
  }

  public float getMaxInput() {
    return maxIn;
  }

  public float getMaxOutput() {
    return maxOut;
  }

  public double getChance() {
    return chance;
  }

  public boolean isPerTick() {
    return isPerTick;
  }
}
